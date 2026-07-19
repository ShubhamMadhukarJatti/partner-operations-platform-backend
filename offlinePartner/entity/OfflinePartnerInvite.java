package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.offlinePartner.model.PartnerGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

@Entity
@Table(name = "Offline_partner_invite")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OfflinePartnerInvite extends BaseEntity {
    private Long organizationId;
    private String email;
    private String status;
    private String remarks;
    private PartnerGroup partnerGroup;
    private String partnerName;
    private boolean verifyEmailSent;
    private String offlinePartnerMessageCode;

    @Column(name = "is_member", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isMember;

    @Transient
    private String userId;


}
