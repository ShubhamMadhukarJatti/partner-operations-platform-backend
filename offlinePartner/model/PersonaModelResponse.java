package com.sharkdom.offlinePartner.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PersonaModelResponse {
    private String companySector;
    private String companySize;
    private String isPartnershipProgram;
    private String marketSegment;
    private String url;
}
