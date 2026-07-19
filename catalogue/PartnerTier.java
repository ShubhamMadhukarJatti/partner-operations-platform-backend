package com.sharkdom.entity.catalogue;


import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_partner_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerTier extends BaseEntity {

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "tier_name")
    private String tierName;

    @Column(name = "price")
    private Long price;

    @Column(name = "currency")
    private String currency;

    @Column(name = "seat_lower")
    private Integer seatLower;

    @Column(name = "seat_upper")
    private Integer seatUpper;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "region")
    private String region;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "is_active",columnDefinition = "boolean default false")
    private Boolean active = false;
}