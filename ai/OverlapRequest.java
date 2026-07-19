package com.sharkdom.model.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OverlapRequest {

    private Long organizationId;

    private PersonaMode persona;

    private OverlapFrequency frequency;

    private RecordType recordType;

    private String fileName;

    private String googleSheetLink;

    /**
     * Mapping like:
     * email -> Email Address
     * domain -> Website
     */
    private Map<String, String> fieldToColumnMapping;

    /**
     * Actual records
     */
    private List<OverlapFieldRequest> fields;
}