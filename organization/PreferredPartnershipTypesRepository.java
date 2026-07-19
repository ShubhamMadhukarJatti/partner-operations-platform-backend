package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.PreferredPartnershipTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreferredPartnershipTypesRepository extends JpaRepository<PreferredPartnershipTypes, Long> {
    /*@Query(value = "select p from PreferredPartnershipTypes p where p.UserFk= :orgId")
    List<PreferredPartnershipTypesModel> findAllByOrganizationId(long orgId);*/

    @Query(value = """
    SELECT id, GROUP_CONCAT(DISTINCT area) AS areas
    FROM (
        SELECT user_fk as id, area FROM org_preferred_partnership_types
        UNION ALL
        SELECT organization_id as id, area FROM org_preferred_sub_sector
        UNION ALL
        SELECT user_fk as id, area FROM org_preferred_sector
    ) AS combined
    WHERE id != :excludedId
    GROUP BY id
    """, nativeQuery = true)
    List<Object[]> findAllGroupedAreasAsMap(@Param("excludedId") Long excludedId);


}
