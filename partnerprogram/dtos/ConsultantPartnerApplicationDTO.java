package com.sharkdom.partnerprogram.dtos;

import com.sharkdom.partnerprogram.enums.*;
import lombok.Data;

@Data
public class ConsultantPartnerApplicationDTO {

    private Long id;
    private String fullName;
    private String linkedinProfileUrl;
    private String email;

    private Country country;
    private RoleDescription roleDescription;
    private B2BAdvisoryCount advisoryCount;
    private ARRRange arrRange;
    private ARRRange typicalClientArrRange;
    private PartnerProgramStatus partnerProgramStatus;
    private LeadSource leadSource;

    private Boolean useDweepBot;
    private Boolean acceptCommissionTerms;
    private Boolean agreeToTerms;
}