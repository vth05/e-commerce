package com.e_commerce.e_commerce.service;

import com.e_commerce.e_commerce.configuration.CustomUserDetails;
import com.e_commerce.e_commerce.entity.User;
import com.e_commerce.e_commerce.exception.AppException;
import com.e_commerce.e_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    UserRepository userRepository;

    // failed to lazily initialize a collection of role: com.e_commerce.e_commerce.entity.User.roles: could not initialize proxy - no Session, so I use Transactional
    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        if (!user.isActive()) {
            throw new DisabledException("The account is blocked and cannot be used to sign in");
        }
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream().map((role) -> new SimpleGrantedAuthority(role.getName())).toList();
        return CustomUserDetails.builder()
                .userId(user.getId())
                .user(user)
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
