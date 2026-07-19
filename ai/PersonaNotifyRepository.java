package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.PersonaNotifyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PersonaNotifyRepository extends JpaRepository<PersonaNotifyEntity, Long> {
    @Query("SELECT COUNT(p) FROM PersonaNotifyEntity p WHERE p.receiverOrganizationId = :receiverOrganizationId " +
            "AND p.creationTimestamp >= :lastWeek AND p.creationTimestamp <= :now")
    long countBySenderIdInLastWeek(
            @Param("receiverOrganizationId") Long receiverOrganizationId,
            @Param("lastWeek") LocalDateTime lastWeek,
            @Param("now") LocalDateTime now
    );
}

