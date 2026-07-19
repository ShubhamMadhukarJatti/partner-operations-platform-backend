package com.sharkdom.repository.admin;

import com.sharkdom.entity.admin.DiscoveryEntity;
import com.sharkdom.model.admin.DiscoveryEntityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscoveryRepository extends JpaRepository<DiscoveryEntity, Long> {

    boolean existsByOrganizationName(String organizationName);

    @Query(value = """
            SELECT organizationName AS organizationName, logoUrl AS logoUrl, FALSE AS existsInOrganization
            FROM DiscoveryEntity
            WHERE organizationName LIKE %:partialUsername%

            UNION

            SELECT name AS organizationName, logoUrl AS logoUrl, TRUE AS existsInOrganization
            FROM Organization
            WHERE name LIKE %:partialUsername% and status = 0
            """)
    Page<DiscoveryEntityResponse> searchOrganization(String partialUsername, Pageable pageable);

}
