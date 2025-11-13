package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.UserUpdateRequest;
import com.e_commerce.e_commerce.entity.Role;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.dto.request.UserCreationRequest;
import com.e_commerce.e_commerce.dto.response.UserResponse;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.mapper.UserMapper;
import com.e_commerce.e_commerce.repository.RoleRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.template.EmailTemplates;
import com.e_commerce.e_commerce.util.SecurityUtils;
import com.e_commerce.e_commerce.util.UserUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
            user.setGender(UserUtils.parseGender(userCreationRequest.getGender()));
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
    public Page<UserResponse> getUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> userMapper.toUserResponse(user));
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public UserResponse getUser(String userId) {
        boolean isAdmin = SecurityUtils.isAdmin();
        User user = getUserByIdWithAccessCheck(userId, isAdmin);
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public UserResponse updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        boolean isAdmin = SecurityUtils.isAdmin();
        User user = getUserByIdWithAccessCheck(userId, isAdmin);
        if (isAdmin) {
            Boolean active = userUpdateRequest.getActive();
            Boolean emailVerified = userUpdateRequest.getEmailVerified();
            if (active != null) {
                user.setActive(active);
            }
            if (emailVerified != null) {
                user.setEmailVerified(emailVerified);
            }
        }
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
            user.setGender(UserUtils.parseGender(userUpdateRequest.getGender()));
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
        boolean isAdmin = SecurityUtils.isAdmin();
        User user = getUserByIdWithAccessCheck(userId, isAdmin);
        // old JWT has been invalidated
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setActive(false);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    private User getUserByIdWithAccessCheck(String userId, boolean isAdmin) {
        User user;
        if (isAdmin) {
            user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        } else {
            user = userRepository.findByIdAndActiveTrue(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        }
        return user;
    }
}
