package com.sharkdom.entity.organizationcollaboration.dto;

import com.sharkdom.entity.organizationcollaboration.SpaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartnerSpaceRequest {

    private String spaceName;
    private SpaceType spaceType;
    private List<Integer> partnerJoined;
    private Long creatorOrgId;
}
