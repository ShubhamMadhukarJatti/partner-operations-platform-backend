package com.sharkdom.agenticai.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "t_partnership_team")
public class PartnerShipTeam extends BaseEntity {

    @Column(name = "member_name", length = 150)
    private String name;

    @Column(name = "linkedin_url", columnDefinition = "TEXT")
    private String linkedin;

    @Column(name = "designation", length = 150)
    private String title;

    @Column(name = "email", length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "partner_company_profile_id", nullable = false)
    private PartnerCompanyProfile partnerCompanyProfile;
}