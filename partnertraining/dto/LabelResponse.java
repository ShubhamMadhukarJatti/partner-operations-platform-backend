package com.sharkdom.partnertraining.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabelResponse {

    private Long id;
    private String name;

}