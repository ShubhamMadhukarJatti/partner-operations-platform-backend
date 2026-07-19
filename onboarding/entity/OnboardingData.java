package com.sharkdom.onboarding.entity;

import com.sharkdom.converter.RegionConverter;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.RegionToPartnerWith;
import com.sharkdom.entity.organization.TeamSize;
import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "t_onboarding_datas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingData extends BaseEntity {

    @Column(name = "company_url", length = 255)
    private String companyURL;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "team_participation", length = 100)
    private String teamParticipation;

    @Column(name = "market_segment", length = 100)
    private String marketSegment;

    @Column(name = "partnership_team_size")
    @Enumerated(EnumType.STRING)
    private TeamSize partnershipTeamSize;

    @Column(name = "current_partners", length = 100)
    private String currentPartners;

    @Column(name = "goals_with_sharkdom")
    private String goalsWithSharkdom;

    @Convert(converter = RegionConverter.class)
    private List<RegionToPartnerWith> regionToPartnerWith;

    // Stores comma-separated values as TEXT
    @Column(name = "preferred_partnerships", columnDefinition = "TEXT")
    private String preferredPartnerships;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    public List<String> getGoalsWithSharkdomAsList() {
        if (goalsWithSharkdom == null || goalsWithSharkdom.isBlank()) {
            return List.of();
        }
        return Arrays.stream(goalsWithSharkdom.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
