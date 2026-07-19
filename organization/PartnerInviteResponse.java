package com.sharkdom.model.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerInviteResponse {
    private Long id;
    private String email;
    private String name;
    private boolean verified;
    private boolean onboarded;
    private String logoUrl;
    private String code;
}