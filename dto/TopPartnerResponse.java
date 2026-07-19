package com.sharkdom.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopPartnerResponse {

    private Long organizationId;
    private Long topPartner;
}
