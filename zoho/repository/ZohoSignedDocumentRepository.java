package com.sharkdom.zoho.repository;

import com.sharkdom.zoho.entity.ZohoSignedDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZohoSignedDocumentRepository extends JpaRepository<ZohoSignedDocumentEntity, Long> {
    ZohoSignedDocumentEntity findByRecipientRequestId(String requestId);

    ZohoSignedDocumentEntity findBySenderRequestId(String requestId);

    ZohoSignedDocumentEntity findByRecipientDocumentId(String documentId);

    List<ZohoSignedDocumentEntity> findAllByOfflinePartnerId(Long id);
    List<ZohoSignedDocumentEntity> findAllByOrganizationCollaborationId(Long id);
}
