package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Offline_partner_documents")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OfflinePartnerDocuments extends BaseEntity {

    private Long organizationId;
    private String email;
    private String pdfUrl;
    private String effectiveDate;
    private String expiringDate;
    private String docId;
    private String count;
}
