package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.PartnershipMouVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository

public interface PartnershipMouVersionRepository extends JpaRepository<PartnershipMouVersion, Long> {

    Page<PartnershipMouVersion> getAllByOrganizationCollaborationId(long organizationCollaborationId, Pageable pageable);

    Optional<PartnershipMouVersion> findTopByOrganizationCollaborationIdOrderByVersionDesc(long organizationCollaborationId);

    @Query("SELECT e.organizationCollaborationId FROM PartnershipMouVersion e WHERE FUNCTION('DATE', e.creationTimestamp) = FUNCTION('DATE', :daysAgo) AND e.status = 0")
    List<Long> findPendingRecordsCreatedDaysAgo(Date daysAgo);

    @Query(value = "Select Count(*) from PartnershipMouVersion where organizationCollaborationId in (:collaborationsIds) AND status = 7 AND creationTimestamp >= :lastWeek AND creationTimestamp <= :now")
    Long getAllActiveCollaborationsInWeek(List<Long> collaborationsIds, @Param("lastWeek") LocalDateTime lastWeek,
                                          @Param("now") LocalDateTime now);
}
