package com.sharkdom.partnerattribution.dto;

import lombok.Data;

import java.util.List;

@Data
public class HubspotDealContactAssociationDto {

    private List<Result> results;

    @Data
    public static class Result {

        private Long toObjectId;

    }

}
