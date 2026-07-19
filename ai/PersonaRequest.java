package com.sharkdom.model.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PersonaRequest {
    private Long organizationId;
    private List<String> sites;
    private String[] names;
    private Frequency frequency;
    private String personaName;
    private String columnName;
    private String googleSheetLink;
    private PersonaMode personaMode;
    private String fileName;
}
