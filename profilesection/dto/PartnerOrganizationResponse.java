package com.sharkdom.profilesection.dto;

import com.sharkdom.partnertraining.dto.LabelResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PartnerOrganizationResponse {

    private Long organizationId;
    private Integer searchCount;
    private List<String> marketSegment;
    private List<LabelResponse> labels;
}