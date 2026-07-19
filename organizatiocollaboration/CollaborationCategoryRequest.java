package com.sharkdom.model.organizatiocollaboration;

import lombok.Data;

import java.util.List;

@Data
public class  CollaborationCategoryRequest {
    private Long organizationId;
    private List<Long> organizationCollaborationId;
    private CollaborationCategory category;
}

