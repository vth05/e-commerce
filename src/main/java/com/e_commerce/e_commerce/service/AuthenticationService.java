package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.configuration.CustomUserDetails;
import com.e_commerce.e_commerce.dto.request.IntrospectRequest;
import com.e_commerce.e_commerce.dto.request.LoginRequest;
import com.e_commerce.e_commerce.dto.request.LogoutRequest;
import com.e_commerce.e_commerce.dto.request.RefreshTokenRequest;
import com.e_commerce.e_commerce.dto.response.IntrospectResponse;
import com.e_commerce.e_commerce.dto.response.LoginResponse;
import com.e_commerce.e_commerce.entity.RedisToken;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.enums.ErrorCode;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.RedisTokenRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    RedisTokenRepository redisTokenRepository;
    AuthenticationManager authenticationManager;

    @NonFinal
    @Value("${jwt.signerKey}")
    String signerKey;
    @NonFinal
    @Value("${jwt.valid-duration}")
    long validDuration;
    @NonFinal
    @Value("${jwt.refreshable-duration}")
    long refreshableDuration;

    public LoginResponse login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("customUserDetails in login method: {}", customUserDetails);
        return LoginResponse.builder()
                .token(generateToken(customUserDetails.getUser()))
                .build();
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("admin")
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("userId", user.getId())
                .claim("tokenVersion", user.getTokenVersion())
                .expirationTime(new Date(Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach((permission) -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }

    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        String token = introspectRequest.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (ParseException | JOSEException | AppException e) {
            // instead of letting GlobalExceptionHandler handle the AppException, the introspect method should catch it and return valid = false
            log.info("introspect method: " + e.getMessage());
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefreshNeeded) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        JWSVerifier jwsVerifier = new MACVerifier(signerKey.getBytes());
        Date expirationTime;
        if (isRefreshNeeded) {
            expirationTime = new Date(jwtClaimsSet.getIssueTime().toInstant().plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli());
        } else {
            expirationTime = jwtClaimsSet.getExpirationTime();
        }
        boolean isVerified = signedJWT.verify(jwsVerifier);
        if (!isVerified || expirationTime.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (redisTokenRepository.existsById(jwtClaimsSet.getJWTID())) {
            log.info("Token has been invalidated (verifyToken method): {}", token);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        User user = userRepository.findByUsername(jwtClaimsSet.getSubject()).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if (!user.isEmailVerified()) {
            log.info("User's emailVerified is false");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (user.getTokenVersion() != ((Number) jwtClaimsSet.getClaim("tokenVersion")).intValue()) {
            log.info("Token version mismatch (verifyToken method): {}", token);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    public void logout(LogoutRequest logoutRequest) {
        try {
            String token = logoutRequest.getToken();
            SignedJWT signedJWT = verifyToken(token, true);
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            redisTokenRepository.save(RedisToken.builder()
                    .jwtId(jwtClaimsSet.getJWTID())
                    .ttl(Duration.between(Instant.now(), jwtClaimsSet.getIssueTime().toInstant().plus(refreshableDuration, ChronoUnit.SECONDS)).getSeconds())
                    .build());
        } catch (ParseException | JOSEException | AppException e) {
            log.info("Token invalid or expired during logout: {}", e.getMessage());
        }
    }

    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            SignedJWT signedJWT = verifyToken(refreshTokenRequest.getToken(), true);
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            redisTokenRepository.save(RedisToken.builder()
                    .jwtId(jwtClaimsSet.getJWTID())
                    .ttl(Duration.between(Instant.now(), jwtClaimsSet.getIssueTime().toInstant().plus(refreshableDuration, ChronoUnit.SECONDS)).getSeconds())
                    .build());
            String username = jwtClaimsSet.getSubject();
            User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
            return LoginResponse.builder()
                    .token(generateToken(user))
                    .build();
        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
