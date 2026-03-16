package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.entity.OtpSession;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.enums.OtpAction;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.OtpSessionRepository;
import com.e_commerce.e_commerce.template.EmailTemplates;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class OtpService {
    OtpSessionRepository otpSessionRepository;
    RedisTemplate<String, String> redisTemplate;
    EmailService emailService;

    public void sendOtpForChangeEmail(User user, String newEmail) {
        String id = UUID.randomUUID().toString();
        String userId = user.getId();
        String indexKey = "OtpIndex:CHANGE_EMAIL:" + userId;
        try {
            if (redisTemplate.hasKey(indexKey)) {
                throw new AppException(ErrorCode.OTP_ALREADY_SENT);
            }
            SecureRandom random = new SecureRandom();
            String otp = String.valueOf(random.nextInt(900000) + 100000); // tạo từ 100000–999999
            OtpSession otpSession = OtpSession.builder()
                    .id(id)
                    .otp(otp)
                    .target(newEmail)
                    .userId(userId)
                    .action(OtpAction.CHANGE_EMAIL)
                    .build();
            otpSessionRepository.save(otpSession);

            redisTemplate.opsForValue().set(indexKey, id, Duration.ofMinutes(5));

            emailService.sendEmail(newEmail, EmailTemplates.OTP_TO_CHANGE_EMAIL_EMAIL_SUBJECT, EmailTemplates.buildOtpToChangeEmailEmail(user.getUsername(), otp));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            otpSessionRepository.deleteById(id);
            redisTemplate.delete(indexKey);
            log.warn("Exception sending OTP to change email email: {}", e.getMessage());
        }
    }

    public void verifyOtpForChangeEmail(String userId, String otp) {
        String indexKey = "OtpIndex:CHANGE_EMAIL:" + userId;
        String id = String.valueOf(redisTemplate.opsForValue().get(indexKey));
        OtpSession otpSession = otpSessionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));
        if (!otp.equals(otpSession.getOtp())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        otpSessionRepository.deleteById(id);
        redisTemplate.delete(indexKey);
    }

    public void sendOtpForForgotPassword(User user) {
        String id = UUID.randomUUID().toString();
        String userId = user.getId();
        String email = user.getEmail();
        String indexKey = "OtpIndex:FORGOT_PASSWORD:" + userId;
        try {
            if (redisTemplate.hasKey(indexKey)) {
                throw new AppException(ErrorCode.OTP_ALREADY_SENT);
            }
            SecureRandom random = new SecureRandom();
            String otp = String.valueOf(random.nextInt(900000) + 100000); // tạo từ 100000–999999
            OtpSession otpSession = OtpSession.builder()
                    .id(id)
                    .otp(otp)
                    .target(email)
                    .userId(userId)
                    .action(OtpAction.FORGOT_PASSWORD)
                    .build();
            otpSessionRepository.save(otpSession);

            redisTemplate.opsForValue().set(indexKey, id, Duration.ofMinutes(5));

            emailService.sendEmail(email, EmailTemplates.OTP_TO_RESET_PASSWORD_EMAIL_SUBJECT, EmailTemplates.buildOtpToResetPasswordEmail(user.getUsername(), otp));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            otpSessionRepository.deleteById(id);
            redisTemplate.delete(indexKey);
            log.warn("Exception sending OTP to reset password email: {}", e.getMessage());
        }
    }

    public void verifyOtpForForgotPassword(String userId, String otp) {
        String indexKey = "OtpIndex:FORGOT_PASSWORD:" + userId;
        String id = String.valueOf(redisTemplate.opsForValue().get(indexKey));
        OtpSession otpSession = otpSessionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));
        if (!otp.equals(otpSession.getOtp())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        otpSessionRepository.deleteById(id);
        redisTemplate.delete(indexKey);
    }
}
