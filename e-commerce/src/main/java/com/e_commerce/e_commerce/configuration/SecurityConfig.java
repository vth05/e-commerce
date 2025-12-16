package com.e_commerce.e_commerce.configuration;

import com.e_commerce.e_commerce.entity.Role;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.repository.RoleRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import com.e_commerce.e_commerce.service.AuthenticationService;
import com.e_commerce.e_commerce.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SecurityConfig {
    String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh-token",
            "/users",
    };
    String[] PUBLIC_GET_ENDPOINTS = {
            "/verify/email/**",
            "/payment/**",
    };
    CustomJwtDecoder customJwtDecoder;
    AuthenticationService authenticationService;
    UserService userService;
    UserRepository userRepository;
    RoleRepository roleRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((req, res, auth) -> {
                            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) auth;
                            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
                            User user = userService.findOrCreateUser(oAuth2User);
                            String token = authenticationService.generateToken(user);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"token\":\"" + token + "\"}");
                        }))
        ;

        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(customJwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
