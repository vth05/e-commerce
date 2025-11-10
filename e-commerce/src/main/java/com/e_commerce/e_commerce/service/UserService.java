package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.dto.request.UserUpdateRequest;
import com.e_commerce.e_commerce.entity.Role;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.dto.request.UserCreationRequest;
import com.e_commerce.e_commerce.dto.response.UserResponse;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.Gender;
import com.e_commerce.e_commerce.mapper.UserMapper;
import com.e_commerce.e_commerce.repository.RoleRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.e_commerce.e_commerce.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
    String welcomeEmail = """
    Hi [User Name],
    
    Welcome to MyShop! 🎉
    
    Thank you for joining our community. We are excited to have you on board.
    
    Here’s what you can do next:
    - Browse our latest products: https://www.myshop.com
    - Check out exclusive offers for new members
    - Manage your profile and preferences
    
    We’re here to make your shopping experience amazing. If you have any questions, feel free to reply to this email.
    
    Happy shopping! 🛍️
    
    Best regards,
    The MyShop Team
    """;

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
        try {
            emailService.sendEmail(user.getEmail(), "Thanks for joining my e-commerce web", welcomeEmail);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        userMapper.updateUser(user, userUpdateRequest);
        String password = userUpdateRequest.getPassword();
        // optional
        if (password != null) {
            // old JWT has been invalidated
            user.setTokenVersion(user.getTokenVersion() + 1);
            user.setPassword(passwordEncoder.encode(password));
        }
        if (userUpdateRequest.getGender() != null) {
            user.setGender(parseGender(userUpdateRequest.getGender()));
        }
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
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
