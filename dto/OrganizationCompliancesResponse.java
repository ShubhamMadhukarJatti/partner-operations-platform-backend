package com.sharkdom.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationCompliancesResponse {

    private Long organizationId;
    private List<String> compliances;
}