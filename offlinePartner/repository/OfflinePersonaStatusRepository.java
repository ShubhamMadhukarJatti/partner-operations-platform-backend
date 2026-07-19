package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.OfflinePersonaStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfflinePersonaStatusRepository extends JpaRepository<OfflinePersonaStatusEntity, Long> {
    OfflinePersonaStatusEntity getByOrganizationIdAndPartnerEmail(Long id, String email);
}
