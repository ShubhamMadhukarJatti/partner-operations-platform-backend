package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.model.organizatiocollaboration.CollaborationRepositoryResponse;
import com.sharkdom.profilesection.dto.PartnerRankingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationCollaborationRepository extends JpaRepository<OrganizationCollaboration, Long> {

    Page<OrganizationCollaboration> getAllBySenderOrganizationId(long senderOrganizationId, Pageable pageable);

    Page<OrganizationCollaboration> getAllByReceiverOrganizationId(long receiverOrganizationId, Pageable pageable);

    @Query(value = "Select Count(*) from OrganizationCollaboration where senderOrganizationId = :organizationId")
    Long countBySenderId(Long organizationId);

    @Query(value = "Select Count(*) from OrganizationCollaboration where receiverOrganizationId = :organizationId")
    Long countByReceiverId(Long organizationId);

    Optional<OrganizationCollaboration> findBySenderOrganizationIdAndReceiverOrganizationIdOrderById(long senderOrganizationId, long receiverOrganizationId);

    Page<OrganizationCollaboration> getAllBySenderOrganizationIdOrReceiverOrganizationIdOrderById(long senderOrganizationId, long receiverOrganizationId, Pageable pageable);

    Optional<OrganizationCollaboration> findFirstBySenderOrganizationIdOrReceiverOrganizationIdOrderByLastUpdatedTimestampDesc(Long senderOrganizationId, Long receiverOrganizationId);

    @Query(value = "Select e.id from OrganizationCollaboration e where receiverOrganizationId = :organizationId or senderOrganizationId = :organizationId")
    List<Long> getAllCollaborations(long organizationId);

    @Query("SELECT oc.receiverOrganizationId AS receiverOrganizationId, oc.senderOrganizationId as senderOrganizationId, oc.id as id, oc.status AS status, oc.creationTimestamp AS creationTimestamp " +
            "FROM OrganizationCollaboration oc " +
            "WHERE oc.senderOrganizationId = :organizationId or oc.receiverOrganizationId = :organizationId ")
    Page<Object[]> getAllPartners(Long organizationId, Pageable pageable);

    @Query("SELECT CASE WHEN o.senderOrganizationId = :id THEN o.receiverOrganizationId ELSE o.senderOrganizationId END AS partnerId " +
            "FROM OrganizationCollaboration o WHERE status = 'ACTIVE' AND (o.senderOrganizationId = :id OR o.receiverOrganizationId = :id)")
    List<Long> findActivePartnerIds(@Param("id") Long orgId);

    @Query("SELECT oc FROM OrganizationCollaboration oc WHERE (oc.senderOrganizationId = :senderOrgId AND oc.receiverOrganizationId = :receiverOrgId) OR " +
            "(oc.senderOrganizationId = :receiverOrgId AND oc.receiverOrganizationId = :senderOrgId)")
    OrganizationCollaboration findBySenderOrganizationIdOrReceiverOrganizationId(Long senderOrgId, Long receiverOrgId);

    @Query("SELECT oc FROM OrganizationCollaboration oc WHERE (status='PENDING' AND oc.receiverOrganizationId = :receiverOrgId)")
    List<OrganizationCollaboration> findAllPendingCollaboration(Long receiverOrgId);

    @Query("SELECT oc FROM OrganizationCollaboration oc WHERE (status='ACTIVE' AND oc.receiverOrganizationId = :receiverOrgId or oc.senderOrganizationId = :receiverOrgId)")
    List<OrganizationCollaboration> findAllActiveCollaboration(Long receiverOrgId);
    @Query(value = "Select Count(*) from OrganizationCollaboration where senderOrganizationId = :organizationId AND creationTimestamp >= :lastWeek AND creationTimestamp <= :now")
    Long countBySenderIdInWeek(Long organizationId, @Param("lastWeek") LocalDateTime lastWeek,
                               @Param("now") LocalDateTime now);

    @Query(value = "Select Count(*) from OrganizationCollaboration where receiverOrganizationId = :organizationId AND creationTimestamp >= :lastWeek AND creationTimestamp <= :now")
    Long countByReceiverIdInWeek(Long organizationId, @Param("lastWeek") LocalDateTime lastWeek,
                                 @Param("now") LocalDateTime now);

    @Query(value = "Select e.id from OrganizationCollaboration e where receiverOrganizationId = :organizationId or senderOrganizationId = :organizationId AND creationTimestamp >= :lastWeek AND creationTimestamp <= :now")
    List<Long> getAllCollaborationsInWeek(long organizationId, @Param("lastWeek") LocalDateTime lastWeek,
                                          @Param("now") LocalDateTime now);


    @Query("SELECT oc.id as id, oc.receiverOrganizationId as organizationId, oc.creationTimestamp as creationTimestamp, oc.status as status FROM OrganizationCollaboration oc WHERE oc.senderOrganizationId = :senderOrganizationId")
    List<CollaborationRepositoryResponse> getPartnerDashboardAllBySenderOrganizationId(Long senderOrganizationId);

    @Query("SELECT oc.id as id, oc.senderOrganizationId as organizationId, oc.creationTimestamp as creationTimestamp, oc.status as status FROM OrganizationCollaboration oc WHERE oc.receiverOrganizationId = :organizationId")
    List<CollaborationRepositoryResponse> getPartnerDashboardAllByReceiverOrganizationId(Long organizationId);

    @Query("SELECT oc FROM OrganizationCollaboration oc WHERE (status='ACTIVE' AND oc.senderOrganizationId = :senderOrganizationId)")
    List<OrganizationCollaboration> findAllActiveCollaborationBySenderOrganizationId(Long senderOrganizationId);

    @Query("SELECT oc FROM OrganizationCollaboration oc WHERE oc.senderOrganizationId = :senderOrganizationId AND oc.status = 'ACTIVE'")
    List<OrganizationCollaboration> getPartnerDashboardAllBySenderOrganization(Long senderOrganizationId);

    @Query("SELECT oc FROM OrganizationCollaboration oc WHERE oc.receiverOrganizationId = :senderOrganizationId AND oc.status = 'ACTIVE'")
    List<OrganizationCollaboration> getPartnerDashboardAllByReceiverOrganization(Long senderOrganizationId);

    @Query("SELECT COUNT(e) FROM OrganizationCollaboration e WHERE e.receiverOrganizationId = :organizationId OR e.senderOrganizationId = :organizationId")
    long countAllCollaborations(@Param("organizationId") long organizationId);


    @Query("SELECT " +
            "CASE " +
            "   WHEN oc.senderOrganizationId = :orgId THEN oc.receiverOrganizationId " +
            "   ELSE oc.senderOrganizationId " +
            "END as partnerId, " +
            "COUNT(oc.id) as totalCount " +
            "FROM OrganizationCollaboration oc " +
            "WHERE oc.senderOrganizationId = :orgId OR oc.receiverOrganizationId = :orgId " +
            "GROUP BY partnerId " +
            "ORDER BY totalCount DESC")
    List<PartnerRankingResponse> getPartnerRanking(@Param("orgId") Long orgId);


}
