package com.sharkdom.entity.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ai.RecordType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "t_persona_overlap_versioning_record",
        indexes = {
                @Index(name = "idx_org_id", columnList = "org_id"),
                @Index(name = "idx_persona_mode", columnList = "persona_mode"),
                @Index(name = "idx_version", columnList = "version")
        }
)
@Getter
@Setter
public class PersonaVersioning extends BaseEntity {

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "persona_mode", nullable = false)
    private PersonaMode personaMode;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name="frequency")
    private OverlapFrequency overlapFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name="recordType")
    private RecordType recordType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "overlap_record", columnDefinition = "json")
    private JsonNode overlapRecord;

    @Column(name = "version_id")
    private Integer versionId;
}