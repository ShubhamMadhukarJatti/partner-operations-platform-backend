package com.sharkdom.agenticai.model;

import lombok.Data;
import java.util.Map;

@Data
public class TrainingExample {

    private String id;
    private String category;

    private Map<String, Object> input;
    private Map<String, Object> output;

    private String reasoning;

}