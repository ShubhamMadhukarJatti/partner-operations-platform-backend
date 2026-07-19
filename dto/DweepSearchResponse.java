package com.sharkdom.dto;

import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationCustomResponse;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class DweepSearchResponse {
    private String message;
    private Page<OrganizationCustomResponse> organization;
}
