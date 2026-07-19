package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.PartnerPortalCourseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerPortalCourseShareRepository
        extends JpaRepository<PartnerPortalCourseShare, Long> {

    Optional<PartnerPortalCourseShare>
    findByCourseIdAndSenderOrganizationIdAndReceiverUserId(
            Long courseId,
            Long senderOrganizationId,
            String receiverUserId
    );

    long countBySenderOrganizationId(Long senderOrganizationId);

    List<PartnerPortalCourseShare> findAllBySenderOrganizationIdAndActiveTrue(
            Long senderOrganizationId
    );

    List<PartnerPortalCourseShare> id(Long id);

    Optional<PartnerPortalCourseShare> findByReceiverUserId(String receiverUserId);
}