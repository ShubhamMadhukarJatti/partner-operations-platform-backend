package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
        name = "PartnerTierResponse",
        description = "Partner Tier response object"
)
public class PartnerTierResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Gold")
    private String tierName;

    @Schema(example = "12999")
    private Long price;

    @Schema(example = "INR")
    private String currency;

    @Schema(example = "100")
    private Integer seatLower;

    @Schema(example = "200")
    private Integer seatUpper;

    @Schema(example = "20")
    private Integer discountPercent;

    @Schema(example = "India")
    private String region;

    @Schema(example = "#FFD700")
    private String colorCode;

    @Schema(
            description = "Whether the tier is active",
            example = "true"
    )
    private Boolean active;
}
