package com.sharkdom.entity.stripe;

import com.sharkdom.constants.stripe.StripePlanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STRIPE_PLAN_CONFIGURATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripePlanConfiguration {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 600)
    private StripePlanType planType;

    @Column(name = "playground_credits", nullable = false)
    private int playgroundCredits;

    @Column(name = "ai_proposal_credits", nullable = false)
    private int aiProposalCredits;

    @Column(name = "collaboration_sent", nullable = false)
    private int collaborationSent;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "price_id", unique = true, nullable = false)
    private String priceId;

    @Column(name = "currency", nullable = false)
    private String currency;

    private Long seat;

    // Static method to convert enum to entity
    public static StripePlanConfiguration fromEnum(StripePlanType type) {
        return StripePlanConfiguration.builder()
                .planType(type)
                .build();
    }

}
