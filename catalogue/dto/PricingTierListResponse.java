package com.sharkdom.entity.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
@Schema(description = "Paginated pricing tier list response")
public class PricingTierListResponse {

    @Schema(description = "Whether plans exist for given org")
    private boolean hasData;

    @Schema(description = "Paginated pricing tier data")
    private Page<PricingTierResponse> plans;
}