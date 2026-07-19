package com.sharkdom.partnerattribution.dto;


import lombok.Data;
import java.util.List;

@Data
public class EnrichmentRequest {
    private String orgName;
    private String orgWebURL;
    private List<String> departments;
}