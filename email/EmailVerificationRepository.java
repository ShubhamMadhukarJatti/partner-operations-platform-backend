package com.sharkdom.repository.email;

import com.sharkdom.entity.email.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByOrganizationIdAndVerificationCode(Long organizationId, String verificationCode);

    Optional<EmailVerification> findByTransactionIdAndVerificationCode(String transactionId, String verificationCode);
}
