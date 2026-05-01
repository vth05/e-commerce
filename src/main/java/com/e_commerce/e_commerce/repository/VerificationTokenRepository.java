package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findByIdAndActiveTrue(String id);
}
