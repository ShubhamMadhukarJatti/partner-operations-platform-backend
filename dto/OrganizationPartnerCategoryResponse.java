package com.sharkdom.dto;

import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationPartnerCategoryResponse {
    private CollaborationCategory category;
    private List<OrganizationPartnerResponse> partners;
}