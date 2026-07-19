package com.sharkdom.model.ai;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class OverlapRecordsRequest {
    private Long organizationId;
    private RecordType recordType;
    private String fileName;
    private PersonaMode source;
    private OverlapFrequency frequency;
    private String googleSheetLink;
    private List<OverlapRecordsField> fields;
    private Map<String, String> fieldToColumnMapping;
    private String userId;
}
