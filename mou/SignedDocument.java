package com.sharkdom.entity.mou;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "signed_document")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignedDocument extends BaseEntity {
    private long organizationCollaborationId;
    private long partnershipMouVersionId;
    private String documentId;
    private String status;
}
