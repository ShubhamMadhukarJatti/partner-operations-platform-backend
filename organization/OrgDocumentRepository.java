package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.OrgDocumentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgDocumentRepository extends JpaRepository<OrgDocumentsEntity, Long> {
    List<OrgDocumentsEntity> findAllByOrganizationId(Long organizationId);
}
