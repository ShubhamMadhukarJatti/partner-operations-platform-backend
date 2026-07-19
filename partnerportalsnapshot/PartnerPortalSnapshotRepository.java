package com.sharkdom.repository.partnerportalsnapshot;

import com.sharkdom.entity.partnerportalsnapshot.PartnerPortalSnapShot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerPortalSnapshotRepository extends JpaRepository<PartnerPortalSnapShot,Long> {

    Optional<PartnerPortalSnapShot> findBySenderOrganizationIdAndReceiverUserId(Long senderOrgId, String receiverUserId);

    List<PartnerPortalSnapShot> findByReceiverUserId(String receiverUserId);

}
