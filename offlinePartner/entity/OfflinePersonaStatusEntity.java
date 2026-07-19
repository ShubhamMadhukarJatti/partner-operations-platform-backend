package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.PersonaStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "offline_persona_status")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflinePersonaStatusEntity extends BaseEntity {
    private Long organizationId;
    private String partnerEmail;
    private PersonaStatus personaStatus;
}
