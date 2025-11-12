package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.entity.VerificationToken;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.e_commerce.e_commerce.repository.VerificationTokenRepository;
import com.e_commerce.e_commerce.template.EmailTemplates;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class VerificationService {
    EmailService emailService;
    VerificationTokenRepository verificationTokenRepository;
    UserRepository userRepository;

    public void sendVerificationEmail(User user) {
        String id = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .id(id)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .user(user)
                .build();
        verificationTokenRepository.save(verificationToken);
        String username = user.getUsername();
        String email = user.getEmail();
        String verificationLink = "http://localhost:8080/verify/email/" + id;
        try {
            emailService.sendEmail(email, EmailTemplates.VERIFICATION_EMAIL_SUBJECT, EmailTemplates.buildVerificationEmail(username, verificationLink));
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
        verificationTokenRepository.save(verificationToken);

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        try {
            emailService.sendEmail(user.getEmail(), EmailTemplates.WELCOME_EMAIL_SUBJECT, EmailTemplates.buildWelcomeEmail(user.getUsername()));
        } catch (Exception e) {
            log.warn("Exception sending welcome email: {}", e.getMessage());
        }
    }
}
