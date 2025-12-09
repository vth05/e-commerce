package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.entity.OtpSession;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.entity.VerificationToken;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.OtpAction;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.OtpSessionRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.e_commerce.e_commerce.repository.VerificationTokenRepository;
import com.e_commerce.e_commerce.template.EmailTemplates;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
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
    OtpSessionRepository otpSessionRepository;
    RedisTemplate<String, String> redisTemplate;

    public void sendVerificationEmail(User user) {
        VerificationToken verificationToken = VerificationToken.builder()
                .expiryDate(LocalDateTime.now().plusHours(24))
                .user(user)
                .build();
        verificationTokenRepository.save(verificationToken);

        String verificationLink = "http://localhost:8080/verify/email/" + user.getId();
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

    public void sendOtpToChangeEmail(User user, String newEmail) {
        try {
            SecureRandom random = new SecureRandom();
            String otp = String.valueOf(random.nextInt(900000) + 100000); // tạo từ 100000–999999
            emailService.sendEmail(newEmail, EmailTemplates.OTP_TO_CHANGE_EMAIL_EMAIL_SUBJECT, EmailTemplates.buildOtpToChangeEmailEmail(user.getUsername(), otp));
            String userId = user.getId();
            String id = UUID.randomUUID().toString();
            OtpSession otpSession = OtpSession.builder()
                    .id(id)
                    .otp(otp)
                    .target(newEmail)
                    .userId(userId)
                    .action(OtpAction.CHANGE_EMAIL)
                    .build();
            otpSessionRepository.save(otpSession);

            String indexKey = "OtpIndex:CHANGE_EMAIL:" + userId + ":" + newEmail + ":" + otp;
            redisTemplate.opsForValue().set(indexKey, id, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.warn("Exception sending OTP to change email email: {}", e.getMessage());
        }
    }

    public boolean verifyOtpForChangeEmail(String otp, String userId, String target) {
        String indexKey = "OtpIndex:CHANGE_EMAIL:" + userId + ":" + target + ":" + otp;
        String id = String.valueOf(redisTemplate.opsForValue().get(indexKey));
        OtpSession otpSession = otpSessionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));
        if (!otpSession.isActive()) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        otpSession.setActive(false);
        otpSessionRepository.save(otpSession);

        redisTemplate.delete(indexKey);
        return true;
    }
}
