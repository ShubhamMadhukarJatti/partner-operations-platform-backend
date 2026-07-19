package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.PartnerInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerInviteRepository extends JpaRepository<PartnerInvite, Long> {
    List<PartnerInvite> findAllByOrganizationId(Long organizationId);

    List<PartnerInvite> findAllByOrganizationIdAndEmail(Long organizationId, String email);

    Optional<PartnerInvite> findByEmail(String email);
}
