package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
@Schema(
        name = "PartnerTierListResponse",
        description = "Paginated list of partner tiers"
)
public class PartnerTierListResponse {

    @Schema(
            description = "Indicates whether any tier data exists",
            example = "true"
    )
    private boolean hasData;

    @Schema(
            description = "Paginated partner tier list"
    )
    private Page<PartnerTierResponse> tiers;
}