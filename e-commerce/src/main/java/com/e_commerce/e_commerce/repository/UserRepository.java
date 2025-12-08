package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndActiveTrue(String userId);

    boolean existsByEmail(String email);
}
