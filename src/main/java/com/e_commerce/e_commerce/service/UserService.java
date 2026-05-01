package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.*;
import com.e_commerce.e_commerce.entity.Role;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.dto.response.UserResponse;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.mapper.UserMapper;
import com.e_commerce.e_commerce.repository.RoleRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.template.EmailTemplates;
import com.e_commerce.e_commerce.util.ParseUtils;
import com.e_commerce.e_commerce.util.SecurityUtils;
import jakarta.transaction.Transactional;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    EmailVerificationService emailVerificationService;
    OtpService otpService;
    AuthenticationService authenticationService;

    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        if (userRepository.existsByEmail(userCreationRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }
        User user = userMapper.toUser(userCreationRequest);
        user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roleRepository.findById("USER").ifPresent(role -> roles.add(role));
        user.setRoles(roles);
        if (userCreationRequest.getGender() != null) {
            user.setGender(ParseUtils.parseGender(userCreationRequest.getGender()));
        }
        // catch unique constraint violation
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        emailVerificationService.sendVerificationEmail(user);

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
        userMapper.updateUser(user, userUpdateRequest);
        if (userUpdateRequest.getGender() != null) {
            user.setGender(ParseUtils.parseGender(userUpdateRequest.getGender()));
        }
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        return userMapper.toUserResponse(user);
    }

    public String changePassword(ChangePasswordRequest request) {
        User user = userRepository.findById(SecurityUtils.getUserIdFromAuthentication()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        // old JWT has been invalidated
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        try {
            emailService.sendEmail(user.getEmail(), EmailTemplates.PASSWORD_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildPasswordChangedEmail(user.getUsername()));
        } catch (Exception e) {
            log.warn("Exception sending password changed email: {}", e.getMessage());
        }

        return "Password changed successfully";
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        otpService.sendOtpForForgotPassword(user);
        return "OTP sent to the email";
    }

    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        otpService.verifyOtpForForgotPassword(user.getId(), request.getOtp());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        return "Reset password successfully";
    }

    public String requestChangeEmail(RequestChangeEmailOtpRequest request) {
        String newEmail = request.getNewEmail();
        if (userRepository.existsByEmail(newEmail)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }
        User user = userRepository.findById(SecurityUtils.getUserIdFromAuthentication()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        otpService.sendOtpForChangeEmail(user, newEmail);
        return "OTP sent to the new email";
    }

    public String changeEmail(ChangeEmailRequest request) {
        String newEmail = request.getNewEmail();
        if (userRepository.existsByEmail(newEmail)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }
        User user = userRepository.findById(SecurityUtils.getUserIdFromAuthentication()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String oldEmail = user.getEmail();
        otpService.verifyOtpForChangeEmail(user.getId(), request.getOtp());
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setEmail(newEmail);
        userRepository.save(user);

        try {
            String username = user.getUsername();
            emailService.sendEmail(oldEmail, EmailTemplates.EMAIL_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildEmailChangedEmail(username, newEmail));
            emailService.sendEmail(newEmail, EmailTemplates.EMAIL_CHANGED_EMAIL_SUBJECT, EmailTemplates.buildEmailChangedEmail(username, newEmail));
        } catch (Exception e) {
            log.warn("Exception sending email changed email: {}", e.getMessage());
        }

        return "Email changed successfully";
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

    @Transactional
    public String findOrCreateUserAndGenerateToken(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Set<Role> roles = new HashSet<>();
        roleRepository.findById("USER").ifPresent(role -> roles.add(role));
        // find (login) or create user (register)
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .username(email)
                    .email(email)
                    .emailVerified(true)
                    .roles(roles)
                    .build();
            return userRepository.save(newUser);
        });
        return authenticationService.generateToken(user);
    }
}
