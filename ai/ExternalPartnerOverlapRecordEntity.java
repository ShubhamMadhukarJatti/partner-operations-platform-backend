package com.sharkdom.entity.ai;

import com.sharkdom.converter.MapToJsonConverter;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ai.RecordType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "t_extnernal_overlap_record")
@Data
public class ExternalPartnerOverlapRecordEntity extends BaseEntity {
    private String userId;

    @Enumerated(EnumType.STRING)
    private RecordType recordType;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private PersonaMode source;

    @Enumerated(EnumType.STRING)
    private OverlapFrequency frequency;

    private String googleSheetLink;

    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, String> fieldToColumnMapping;

    @OneToMany(mappedBy = "overlapRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExternalPartnerOverlapRecordFieldEntity> fields = new ArrayList<>();
}

