package com.sharkdom.partnerattribution.dto;

import com.sharkdom.partnerattribution.enums.ContactSource;
import lombok.Data;

@Data
public class SharedContactRequestDTO {

    private Long partnerOrgId;
    private String name;
    private String title;
    private ContactSource source;
    private String relationship;
    private Boolean inCrm;
    private String dealId;
}
