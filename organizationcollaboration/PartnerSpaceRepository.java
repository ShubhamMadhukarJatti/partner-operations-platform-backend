package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.PartnerSpaceRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerSpaceRepository extends JpaRepository<PartnerSpaceRoom, Long> {
    Optional<PartnerSpaceRoom> findByChatRoomId(Long chatRoomId);

    @Query("SELECT p FROM PartnerSpaceRoom p WHERE " + "p.partnerCreated = :orgId OR " + ":orgIdInt MEMBER OF p.partnerJoined")
    List<PartnerSpaceRoom> findAllByOrganizationId(@Param("orgId") Long orgId, @Param("orgIdInt") Integer orgIdInt);

    List<PartnerSpaceRoom> findAllByPartnerCreated(Long orgId);
}
