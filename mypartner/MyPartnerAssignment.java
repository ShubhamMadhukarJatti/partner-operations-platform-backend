package com.sharkdom.entity.mypartner;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="t_my_partner_assignment")
public class MyPartnerAssignment extends BaseEntity {

    @Column(name="USER_ID")
    private String userId;

    @Column(name = "ORG_ID")
    private Long organizationId;

    @Column(name = "PARTNER_ORG_ID")
    private Long partnerOrgId;
}
