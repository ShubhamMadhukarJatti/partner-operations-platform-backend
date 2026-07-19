package com.sharkdom.agenticai.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CompanyDiscoveryResult {
    private String            companyName;
    private String            industry;
    private String            location;
    private Integer           employees;
    private String            linkedinUrl;
    private List<ContactPerson> contactPersons;
}