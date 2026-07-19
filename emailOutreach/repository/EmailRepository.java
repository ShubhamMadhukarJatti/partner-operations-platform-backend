package com.sharkdom.emailOutreach.repository;

import com.sharkdom.emailOutreach.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findByOrgId(Long aLong);

    List<Email> findByOrgIdAndTo(Long orgId, String to);

    List<Email> findByPartnerOrgId(Long orgId);

    List<Email> findByIsExternalPartnerTrueAndOrgIdAndExternalPartnerCode(Long orgId, String externalPartnerCode);

    Optional<Email> findByThreadId(String threadId);
}
