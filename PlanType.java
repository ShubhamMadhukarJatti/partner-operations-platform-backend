package com.sharkdom.constants;

import lombok.Getter;

@Getter
public enum PlanType {
    FREE(2, 1, 4, 200),
    STANDARD(3, 2, 6, 64900),
    PREMIUM(7, 5, 10, 249900),
    ELITE(9, 9, 15, 479900),
    STANDARD_PREMIUM(7, 5, 10, 249900),
    PREMIUM_ELITE(9, 9, 15, 479900),
    STANDARD_ELITE(9, 9, 15, 479900),
    STANDARD_YEARLY(36, 24, 72, 699000),
    PREMIUM_YEARLY(84, 60, 120, 2699000),
    ELITE_YEARLY(108, 108, 180, 5199000),
    STANDARD_TRIAL(3, 2, 6, 46695),
    PREMIUM_TRIAL(7, 5, 10, 98940);

    private final int playgroundCredits;
    private final int aiProposalCredits;
    private final int collaborationSent;
    private final int amount;

    PlanType(int playgroundCredits, int aiProposalCredits, int collaborationSent, int amount) {
        this.playgroundCredits = playgroundCredits;
        this.aiProposalCredits = aiProposalCredits;
        this.collaborationSent = collaborationSent;
        this.amount = amount;
    }


}
