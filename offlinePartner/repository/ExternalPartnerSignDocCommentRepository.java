package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.ExternalPartnerSignDocComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalPartnerSignDocCommentRepository extends JpaRepository<ExternalPartnerSignDocComment,Long> {

    Optional<ExternalPartnerSignDocComment> findByIdAndOrgId(
            Long id,
            Long orgId
    );

    List<ExternalPartnerSignDocComment> findAllByOrgIdAndExternalPartnerCode(
            Long orgId,
            String externalPartnerCode
    );
}
