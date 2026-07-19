package com.sharkdom.partnerattribution.entities;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sales_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesProfile extends BaseEntity {

    @Column(name = "full_name")
    private String name;

    @Column(name = "designation")
    private String role;

    @Column(name = "active_deals")
    private Integer activeDeals;

    @Column(name = "win_rate")
    private Double winRate;

    @Column(name = "average_cycle_days")
    private Integer avgCycleDays;

    @Column(name = "territory")
    private String territory;

    @Column(name = "territory_matched")
    private Boolean territoryMatched;

    @Column(name="org_id")
    private Long orgId;
}