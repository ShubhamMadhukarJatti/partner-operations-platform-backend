package com.sharkdom.entity.externalpartner;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="t_external_partner_assignment")
public class ExternalPartnerAssignment extends BaseEntity {

    @Column(name="USER_ID")
    private String userId;

    @Column(name = "ORG_ID")
    private Long organizationId;

    @Column(name = "EXTERNAL_PARTNER_ORG_ID")
    private Long externalPartnerId;

    @Column(name="username")
    private String username;
}
