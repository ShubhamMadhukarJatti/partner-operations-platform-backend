package com.sharkdom.offlinePartner.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PartnerPersonaRequest {
    private Long organizationId;
    private String partnerEmail;
    private List<String> sites;
}
