package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.OfflinePersonaDetailsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfflinePersonaDetailsRepository extends JpaRepository<OfflinePersonaDetailsEntity, Long> {
    Page<OfflinePersonaDetailsEntity> getAllByOrganizationIdAndPartnerEmail(Long id, String partnerEmail, Pageable pageable);
}
