package com.sharkdom.quickstart.repository;

import com.sharkdom.quickstart.dto.RewardType;
import com.sharkdom.quickstart.entity.QuickStartRewardAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuickStartRewardAssignmentRepository extends JpaRepository<QuickStartRewardAssignment, Long> {

    boolean existsByOrganizationIdAndRewardType(Long organizationId, RewardType rewardType);

    List<QuickStartRewardAssignment> findByOrganizationId(Long organizationId);

    @Query("SELECT COUNT(a) FROM QuickStartRewardAssignment a WHERE a.organizationId = :organizationId")
    long countByOrganizationId(Long organizationId);
}
