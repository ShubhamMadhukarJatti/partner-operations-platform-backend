package com.sharkdom.entity.organization;

import com.sharkdom.model.organization.OrganizationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortlistedOrganizationResponse {
    private Long shortlistedOrgId;
    private String shortlistedByUserId;
    private String shortlistedByUserName;
    private Long shortlistedByOrgId;
    private String remark;
    private String logoUrl;
    private String name;
    private Date creationTimestamp;
}