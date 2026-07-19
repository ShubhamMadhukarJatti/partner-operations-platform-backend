package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "getting_started")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GettingStartedEntity extends BaseEntity {
    private Long organizationId;
    private boolean proposalSetupStatus;
    private int proposalSetupProgress;
    private NotFiled inHouseTeam;
    private NotFiled apiProgram;
    private String brandingPage;
    private String activePartnerProgram;
    private String currentPartnerCount;

    public static enum NotFiled {
        YES,
        NO,
        NOT_FILLED
    }

}

