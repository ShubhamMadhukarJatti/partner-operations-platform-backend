package com.sharkdom.entity.ai;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;

@Entity
@Table(name = "persona")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long organizationId;
    private String attribute;
    private String category;
    private Double percentage;

    private Integer versionId;

    private Integer version;

}
