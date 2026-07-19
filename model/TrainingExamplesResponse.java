package com.sharkdom.agenticai.model;


import lombok.Data;
import java.util.List;

@Data
public class TrainingExamplesResponse {

    private Integer count;
    private List<TrainingExample> examples;

}
