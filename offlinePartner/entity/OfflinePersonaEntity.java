package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;

@Entity
@Table(name = "offline_persona")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflinePersonaEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long organizationId;
    private String partnerEmail;
    private String attribute;
    private String category;
    private Double percentage;

}
