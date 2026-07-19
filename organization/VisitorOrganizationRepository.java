package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.VisitorOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitorOrganizationRepository extends JpaRepository<VisitorOrganization, Long> {
    @Query("SELECT m.visitorId FROM VisitorOrganization m WHERE  m.organizationId = :orgId AND m.creationTimestamp >= :lastWeek AND m.creationTimestamp <= :now")
    List<Long> visitorsInLastWeek(@Param("orgId") Long orgId, @Param("lastWeek") LocalDateTime lastWeek,
                                  @Param("now") LocalDateTime now);
}
