package com.sharkdom.model.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripePlanConfigurationResponse {

    private String planType;

    private Integer playgroundCredits;

    private Integer aiProposalCredits;

    private Integer collaborationSent;

    private Long seat;

    private Double amount;

    private String priceId;

    private String currency;

}
