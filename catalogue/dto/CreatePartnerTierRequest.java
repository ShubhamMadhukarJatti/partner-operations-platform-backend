package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "CreatePartnerTierRequest",
        description = "Request payload to create a new Partner Tier"
)
public class CreatePartnerTierRequest {

    @Schema(
            description = "Name of partner tier",
            example = "Gold"
    )
    private String tierName;

    @Schema(
            description = "Price of the tier",
            example = "12999"
    )
    private Long price;

    @Schema(
            description = "Currency code",
            example = "INR"
    )
    private String currency;

    @Schema(
            description = "Minimum number of seats",
            example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer seatLower;

    @Schema(
            description = "Maximum number of seats",
            example = "200",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer seatUpper;

    @Schema(
            description = "Discount percentage",
            example = "20"
    )
    private Integer discountPercent;

    @Schema(
            description = "Applicable region",
            example = "India"
    )
    private String region;

    @Schema(
            description = "UI color code for tier",
            example = "#FFD700"
    )
    private String colorCode;
}