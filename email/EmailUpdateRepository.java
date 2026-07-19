package com.sharkdom.repository.email;

import com.sharkdom.entity.email.EmailUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailUpdateRepository extends JpaRepository<EmailUpdateEntity, Long> {
    Optional<EmailUpdateEntity> findByOriginalEmailAndOtp(String email, String otp);
}
