package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "UpdatePartnerTierRequest",
        description = "Request payload to update an existing Partner Tier"
)
public class UpdatePartnerTierRequest {

    @Schema(example = "Platinum")
    private String tierName;

    @Schema(example = "19999")
    private Long price;

    @Schema(example = "INR")
    private String currency;

    @Schema(example = "200")
    private Integer seatLower;

    @Schema(example = "400")
    private Integer seatUpper;

    @Schema(example = "25")
    private Integer discountPercent;

    @Schema(example = "India")
    private String region;

    @Schema(example = "#C0C0C0")
    private String colorCode;
}