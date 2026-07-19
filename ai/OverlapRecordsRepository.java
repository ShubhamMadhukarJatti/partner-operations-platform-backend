package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ai.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OverlapRecordsRepository extends JpaRepository<OverlapRecordEntity, Long> {
    List<OverlapRecordEntity> findByOrganizationId(Long organizationId);

    Page<OverlapRecordEntity> findPageByOrganizationId(Long organizationId, Pageable pageable);

    Page<OverlapRecordEntity> findPageByOrganizationIdAndRecordType(Long organizationId, RecordType recordType, Pageable pageable);

    Optional<OverlapRecordEntity> findByOrganizationIdAndRecordType(Long organizationId, RecordType recordType);

    boolean existsByOrganizationId(Long organizationId);

    @Query("SELECT DISTINCT o.organizationId FROM OverlapRecordEntity o WHERE o.organizationId IS NOT NULL")
    List<Long> findDistinctOrganizationIds();

    Optional<OverlapRecordEntity>
    findTopByOrganizationIdAndSourceAndFrequencyOrderByVersionDesc(
            Long organizationId,
            PersonaMode source,
            OverlapFrequency frequency
    );

    List<OverlapRecordEntity> findByOrganizationIdAndSource(Long organizationId, PersonaMode personaMode);

    @Query("""
        SELECT MAX(o.version)
        FROM OverlapRecordEntity o
        WHERE o.organizationId = :orgId
        AND o.source = :source
    """)
    Integer findMaxVersion(Long orgId, PersonaMode source);

    Optional<OverlapRecordEntity>
    findTopByOrganizationIdAndSourceOrderByVersionDesc(
            Long orgId,
            PersonaMode source
    );

    Optional<OverlapRecordEntity>
    findTopByOrganizationIdOrderByVersionDesc(
            Long orgId
    );

    List<OverlapRecordEntity> findByOrganizationIdAndVersionId(
            Long organizationId,
            Integer versionId
    );

    List<OverlapRecordEntity> findByOrganizationIdAndRecordTypeAndVersionId(
            Long organizationId,
            RecordType recordType,
            Integer versionId
    );

    Optional<OverlapRecordEntity>
    findTopByOrganizationIdAndRecordTypeOrderByVersionDesc(
            Long organizationId,
            RecordType recordType
    );

    @Query("""
        SELECT f
        FROM OverlapRecordFieldEntity f
        WHERE f.dealId = :dealId
    """)
    Optional<OverlapRecordFieldEntity> findOverlapRecordFieldByDealId(
            @Param("dealId") String dealId
    );


    @Query("""
SELECT r FROM OverlapRecordEntity r
WHERE r.version = (
   SELECT MAX(o.version)
   FROM OverlapRecordEntity o
   WHERE o.organizationId = r.organizationId
   AND o.recordType = :recordtype
)
""")
    List<OverlapRecordEntity> findLatestVersionPerOrgAndRecordType(RecordType recordtype);

    @Query("SELECT o FROM OverlapRecordEntity o WHERE o.organizationId = :organizationId AND o.recordType = :recordType")
    List<OverlapRecordEntity> findOverlapRecords(@Param("organizationId") Long organizationId,
                                                 @Param("recordType") RecordType recordType);

}
