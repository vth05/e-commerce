package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.entity.VerificationToken;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.VerificationTokenRepository;
import com.e_commerce.e_commerce.template.EmailTemplates;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class EmailVerificationService {
    EmailService emailService;
    VerificationTokenRepository verificationTokenRepository;
    @NonFinal
    @Value("${app.base-url}")
    String baseUrl;

    public void sendVerificationEmail(User user) {
        VerificationToken verificationToken = VerificationToken.builder()
                .expiryDate(LocalDateTime.now().plusHours(24))
                .user(user)
                .build();
        verificationTokenRepository.save(verificationToken);

        String verificationLink = baseUrl + "/verify/email/" + user.getId();
        try {
            emailService.sendEmail(user.getEmail(), EmailTemplates.VERIFICATION_EMAIL_SUBJECT, EmailTemplates.buildVerificationEmail(user.getUsername(), verificationLink));
            log.info("Verification email: {}", verificationLink);
        } catch (Exception e) {
            log.warn("Exception sending verification email: {}", e.getMessage());
        }
    }

    @Transactional
    public void verifyEmail(String id) {
        VerificationToken verificationToken = verificationTokenRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }
        verificationToken.setActive(false);

        User user = verificationToken.getUser();
        user.setEmailVerified(true);

        try {
            emailService.sendEmail(user.getEmail(), EmailTemplates.WELCOME_EMAIL_SUBJECT, EmailTemplates.buildWelcomeEmail(user.getUsername()));
        } catch (Exception e) {
            log.warn("Exception sending welcome email: {}", e.getMessage());
        }
    }
}
