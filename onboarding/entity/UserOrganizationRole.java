package com.sharkdom.onboarding.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_user_org_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationRole extends BaseEntity {

    private String userId;

    private Long orgId;

    @Column(nullable = false)
    private Boolean isVendor = false;

    @Column(nullable = false)
    private Boolean isPartner = false;
}
