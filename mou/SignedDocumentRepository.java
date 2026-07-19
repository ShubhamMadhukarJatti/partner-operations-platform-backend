package com.sharkdom.repository.mou;

import com.sharkdom.entity.mou.SignedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignedDocumentRepository extends JpaRepository<SignedDocument, Long> {
    SignedDocument findByDocumentId(String documentId);

}
