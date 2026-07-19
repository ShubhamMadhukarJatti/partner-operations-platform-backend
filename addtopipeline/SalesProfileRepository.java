package com.sharkdom.partnerattribution.addtopipeline;

import com.sharkdom.partnerattribution.entities.SalesProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesProfileRepository extends JpaRepository<SalesProfile, Long> {

    List<SalesProfile> findAllByOrgId(Long orgId);
}