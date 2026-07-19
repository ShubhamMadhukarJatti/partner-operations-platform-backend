package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.OrganizationCollaborationCategoryEntity;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface OrganizationCollaborationCategoryRepository extends JpaRepository<OrganizationCollaborationCategoryEntity, Long> {
    @Query("SELECT o.category FROM OrganizationCollaborationCategoryEntity o WHERE o.organizationCollaborationId = :organizationCollaborationId and o.organizationId = :organizationId")
    CollaborationCategory findCategoryByOrganizationCollaborationIdAndOrganizationId(Long organizationCollaborationId, Long organizationId);

    @Query("SELECT o FROM OrganizationCollaborationCategoryEntity o WHERE o.organizationId = :organizationId AND o.organizationCollaborationId = :organizationCollaborationId")
    Optional<OrganizationCollaborationCategoryEntity> findByOrganizationIdAndOrganizationCollaborationIdAndCategory(
            @Param("organizationId") Long organizationId,
            @Param("organizationCollaborationId") Long organizationCollaborationId);

    List<OrganizationCollaborationCategoryEntity> findByOrganizationIdAndCategory(Long orgId,CollaborationCategory category);

}
