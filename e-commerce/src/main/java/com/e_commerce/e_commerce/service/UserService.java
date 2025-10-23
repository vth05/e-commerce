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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
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

    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        User user = userMapper.createUser(userCreationRequest);
        user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roleRepository.findById("USER").ifPresent(role -> roles.add(role));
        user.setRoles(roles);
        // catch unique constraint violation
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map((user) -> userMapper.toUserResponse(user)).toList();
    }

    @PostAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public UserResponse getUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public UserResponse updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, userUpdateRequest);
        // if it is null, this is an optional update
        String password = userUpdateRequest.getPassword();
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['userId']")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
