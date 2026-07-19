package com.sharkdom.model.stripe;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripePlanConfigurationRequest {

    private Integer playgroundCredits;

    private Integer aiProposalCredits;

    private Integer collaborationSent;

    private Long seat;

    @NotBlank
    private String priceId;

}
