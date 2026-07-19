package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.OfflinePartnerDocuments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfflinePartnerDocumentsRepository extends JpaRepository<OfflinePartnerDocuments, Long> {
    Optional<OfflinePartnerDocuments> findByOrganizationIdAndEmail(Long organizationId, String email);

    List<OfflinePartnerDocuments> findAllByOrganizationIdAndEmail(Long organizationId, String email);
}
