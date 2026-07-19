package com.sharkdom.partnerattribution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubspotDealCompanyAssociationDto {

    private List<Result> results;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        private Long toObjectId;

        private List<AssociationType> associationTypes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssociationType {

        private String category;

        private Long typeId;

        private String label;
    }
}