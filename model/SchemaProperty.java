package com.sharkdom.agenticai.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SchemaProperty {

    private Object defaultValue;

    private String title;

    private String type;

    private List<Map<String, String>> anyOf;

    private Map<String, String> items;
}