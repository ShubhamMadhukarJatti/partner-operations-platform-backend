package com.sharkdom.model.ppi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class AzureCustomDomainStatusResponse {
    private String id;
    private String type;
    private String name;
    private Properties properties;
}