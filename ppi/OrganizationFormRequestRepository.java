package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.OrganizationFormRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationFormRequestRepository
        extends JpaRepository<OrganizationFormRequest, Long> {

    Optional<OrganizationFormRequest>
    findBySenderOrgIdAndReceiverOrgId(Long senderOrgId, Long receiverOrgId);

    Optional<OrganizationFormRequest>
    findByFormIdAndSenderOrgId(String formId, Long senderOrgId);
}