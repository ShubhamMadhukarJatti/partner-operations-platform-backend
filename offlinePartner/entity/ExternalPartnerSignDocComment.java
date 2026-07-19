package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_external_partner_doc_comments")
public class ExternalPartnerSignDocComment extends BaseEntity {


    @Column(name = "external_partner_code", nullable = false)
    private String externalPartnerCode;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String commentText;

}
