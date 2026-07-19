package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.OfflinePartnerInvite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OfflinePartnerInviteRepository extends JpaRepository<OfflinePartnerInvite, Long> {

    List<OfflinePartnerInvite> findByOrganizationId(Long organizationId);

    Optional<OfflinePartnerInvite> findByEmail(String email);

    Optional<OfflinePartnerInvite> findByEmailAndOrganizationId(String email, Long organizationId);


    List<OfflinePartnerInvite> findByOrganizationIdAndStatus(Long organizationId,String status);

    List<OfflinePartnerInvite> findByOrganizationIdAndEmailIn(Long organizationId, List<String> emails);

    Optional<OfflinePartnerInvite> findByOrganizationIdAndEmail(Long organizationId, String email);

    Optional<OfflinePartnerInvite> findByOfflinePartnerMessageCode(String code);

    @Query("SELECT o FROM OfflinePartnerInvite o WHERE o.organizationId = :organizationId")
    Page<OfflinePartnerInvite> getOfflinePartnersByOrgId(
            @Param("organizationId") Long organizationId,
            Pageable pageable);

    @Query("SELECT o FROM OfflinePartnerInvite o WHERE o.organizationId = :organizationId AND o.status = :status")
    Page<OfflinePartnerInvite> getOfflinePartnersByOrgIdAndStatus(
            @Param("organizationId") Long organizationId,
            @Param("status") String status,
            Pageable pageable);

    List<OfflinePartnerInvite> findAllByOrganizationIdIsNotNull();

}
