package com.sharkdom.agenticai.repository;

import com.sharkdom.agenticai.entity.PartnerShipTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerShipTeamRepository
        extends JpaRepository<PartnerShipTeam, Long> {

    // Get all team members for a company
    List<PartnerShipTeam> findByPartnerCompanyProfileId(Long partnerCompanyProfileId);

    // Count members (useful for max 2 validation)
    long countByPartnerCompanyProfileId(Long partnerCompanyProfileId);
}
