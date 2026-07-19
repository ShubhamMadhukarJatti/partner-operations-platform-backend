package com.sharkdom.agenticai.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "t_partner_company_profile")
public class PartnerCompanyProfile extends BaseEntity {

    @Column(name = "avg_partner_source_revenue", columnDefinition = "TEXT")
    private String avgPartnerSourceRevenue;

    @Column(name = "partner_ship_team_size")
    private Long partnerShipTeamSize;

    @OneToMany(
            mappedBy = "partnerCompanyProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PartnerShipTeam> partnerShipTeam;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "subsectors", columnDefinition = "TEXT")
    private String subsectors;

    @Column(name = "compliances", columnDefinition = "TEXT")
    private String compliances;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "website", columnDefinition = "TEXT")
    private String website;

    @Column(name = "partner_range", columnDefinition = "TEXT")
    private String partnerRange;
}