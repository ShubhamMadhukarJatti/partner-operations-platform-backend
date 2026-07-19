package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;

@Entity
@Table(name = "offline_persona_details")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflinePersonaDetailsEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private String companySector;
    private String companySize;
    private String isPartnershipProgram;
    private String marketSegment;
    private Long organizationId;
    private String partnerEmail;
}
