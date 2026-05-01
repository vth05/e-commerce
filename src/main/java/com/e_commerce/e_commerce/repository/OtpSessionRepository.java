package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.entity.OtpSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpSessionRepository extends CrudRepository<OtpSession, String> {
}
