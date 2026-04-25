package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.service.EmailVerificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verify")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailVerificationController {
    EmailVerificationService emailVerificationService;

    @GetMapping("/email/{id}")
    ApiResponse<Void> verifyEmail(@PathVariable String id) {
        emailVerificationService.verifyEmail(id);
        return ApiResponse.<Void>builder()
                .message("Email verified successfully!")
                .build();
    }
}
