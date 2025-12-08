package com.e_commerce.e_commerce.entity;

import com.e_commerce.e_commerce.enums.OtpAction;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisHash;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "OtpSession", timeToLive = 300)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OtpSession {
    @Id
    String id;

    String otp;

    String target;

    String userId;

    OtpAction action;

    @Builder.Default
    boolean active = true;
}
