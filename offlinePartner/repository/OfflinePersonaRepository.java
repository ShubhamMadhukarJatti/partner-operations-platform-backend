package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.OfflinePersonaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfflinePersonaRepository extends JpaRepository<OfflinePersonaEntity, Long> {
    List<OfflinePersonaEntity> getAllByOrganizationIdAndPartnerEmail(Long id, String email);

    Optional<OfflinePersonaEntity> findFirstByOrganizationIdAndPartnerEmail(Long id, String email);
}
