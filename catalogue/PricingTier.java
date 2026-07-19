package com.sharkdom.entity.catalogue;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_pricing_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingTier extends BaseEntity {

    @Column(name = "tier_name", nullable = false)
    private String tierName;

    @Column(nullable = false)
    private BigDecimal price;

    private String currency;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "is_active",columnDefinition = "boolean default false")
    private Boolean active = false;

    @ManyToMany
    @JoinTable(
            name = "pricing_tier_features",
            joinColumns = @JoinColumn(name = "pricing_tier_id"),
            inverseJoinColumns = @JoinColumn(name = "pricing_feature_id")
    )
    private Set<PricingFeature> features = new HashSet<>();

    private Long orgId;
}

