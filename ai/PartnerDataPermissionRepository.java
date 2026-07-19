package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.PartnerDataPermissionEntity;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerDataPermissionRepository extends JpaRepository<PartnerDataPermissionEntity, Long> {
    List<PartnerDataPermissionEntity> findByOrganizationId(Long organizationId);

    Optional<PartnerDataPermissionEntity> findByOrganizationIdAndCollaborationCategory(Long organizationId, CollaborationCategory collaborationCategory);

} 