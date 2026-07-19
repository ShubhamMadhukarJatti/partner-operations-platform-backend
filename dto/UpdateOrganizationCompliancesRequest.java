package com.sharkdom.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrganizationCompliancesRequest {

    private List<String> compliances;
}