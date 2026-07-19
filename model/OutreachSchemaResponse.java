package com.sharkdom.agenticai.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class OutreachSchemaResponse {

    private String description;

    private Map<String, SchemaProperty> properties;

    private String title;

    private String type;
}