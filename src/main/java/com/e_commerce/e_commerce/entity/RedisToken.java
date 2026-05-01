package com.e_commerce.e_commerce.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RedisHash")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RedisToken {
    // from data.annotation library
    @Id
    String jwtId;

    // default unit is seconds
    @TimeToLive
    Long ttl;
}
