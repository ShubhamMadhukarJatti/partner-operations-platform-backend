package com.sharkdom.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerPortalBrandingRequest {
    private String title;
    private String description;
    private String url;
    private boolean enabledReferralProgram;
}