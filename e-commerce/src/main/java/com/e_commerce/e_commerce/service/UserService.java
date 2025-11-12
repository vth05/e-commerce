package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.UserUpdateRequest;
import com.e_commerce.e_commerce.entity.Role;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.dto.request.UserCreationRequest;
import com.e_commerce.e_commerce.dto.response.UserResponse;
import com.e_commerce.e_commerce.entity.VerificationToken;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.Gender;
import com.e_commerce.e_commerce.mapper.UserMapper;
import com.e_commerce.e_commerce.repository.RoleRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.template.EmailTemplates;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    VerificationService verificationService;

    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        User user = userMapper.toUser(userCreationRequest);
        user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roleRepository.findById("USER").ifPresent(role -> roles.add(role));
        user.setRoles(roles);
        if (userCreationRequest.getGender() != null) {
            user.setGender(parseGender(userCreationRequest.getGender()));
        }
        // catch unique constraint violation
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        verificationService.sendVerificationEmail(user);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(user -> userMapper.toUserResponse(user)).toList();
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public UserResponse getUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public UserResponse updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String oldEmail = user.getEmail();
        userMapper.updateUser(user, userUpdateRequest);
        String newPassword = userUpdateRequest.getPassword();
        // optional
        if (newPassword != null) {
            // old JWT has been invalidated
            user.setTokenVersion(user.getTokenVersion() + 1);
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        if (userUpdateRequest.getGender() != null) {
            user.setGender(parseGender(userUpdateRequest.getGender()));
        }
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        // username may change or not
        String newUsername = user.getUsername();
        String newEmail = userUpdateRequest.getEmail();
        if (newPassword != null) {
            try {
                if (newEmail != null && !newEmail.equals(oldEmail)) {
                    emailService.sendEmail(oldEmail, EmailTemplates.PASSWORD_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildPasswordChangedEmail(newUsername));
                    emailService.sendEmail(newEmail, EmailTemplates.PASSWORD_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildPasswordChangedEmail(newUsername));
                } else {
                    emailService.sendEmail(oldEmail, EmailTemplates.PASSWORD_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildPasswordChangedEmail(newUsername));
                }
            } catch (Exception e) {
                log.warn("Exception sending password changed email: {}", e.getMessage());
            }
        }
        if (newEmail != null && !newEmail.equals(oldEmail)) {
            try {
                emailService.sendEmail(oldEmail, EmailTemplates.EMAIL_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildEmailChangedEmail(newUsername, newEmail));
                emailService.sendEmail(newEmail, EmailTemplates.EMAIL_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildEmailChangedEmail(newUsername, newEmail));
            } catch (Exception e) {
                log.warn("Exception sending email changed email: {}", e.getMessage());
            }
        }
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public UserResponse deactivateUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // old JWT has been invalidated
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setActive(false);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    private Gender parseGender(String genderStr) {
        try {
            return Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_GENDER);
        }
    }
}
