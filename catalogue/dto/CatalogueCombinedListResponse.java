package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
@Schema(description = "Combined paginated response for Pricing & Partner tiers")
public class CatalogueCombinedListResponse {

    @Schema(
            description = "True if either pricing tiers or partner tiers have data",
            example = "true"
    )
    private boolean hasData;

    @Schema(description = "Pricing tier paginated data")
    private Page<PricingTierResponse> pricingTiers;

    @Schema(description = "Partner tier paginated data")
    private Page<PartnerTierResponse> partnerTiers;
}