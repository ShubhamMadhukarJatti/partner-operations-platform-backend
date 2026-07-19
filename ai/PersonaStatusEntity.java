package com.sharkdom.entity.ai;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.Frequency;
import com.sharkdom.model.ai.PersonaMode;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "persona_status")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaStatusEntity extends BaseEntity {

    private Long organizationId;

    private PersonaStatus personaStatus;

    private Frequency frequency;

    private String columnName;

    private String fileName;

    private PersonaMode personaMode;

    private String googleSheetLink;

    private Integer versionId;

    private Integer version;
}
