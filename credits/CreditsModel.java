package com.sharkdom.model.credits;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditsModel {
    private Long organizationId;
    private int playgroundCredits;
    private int aiProposalCredits;
    private Long collaborationSent;
    private Long collaborationAccepted;
}
