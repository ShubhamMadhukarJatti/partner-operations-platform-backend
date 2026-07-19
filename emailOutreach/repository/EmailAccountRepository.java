package com.sharkdom.emailOutreach.repository;

import com.sharkdom.emailOutreach.entity.EmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailAccountRepository extends JpaRepository<EmailAccount, Long> {

    Optional<EmailAccount> findByOrganizationId(Long organizationId);
}
