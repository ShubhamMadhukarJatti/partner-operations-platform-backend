package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_organization_partner_program_benefits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationPartnerProgramBenefit extends BaseEntity {

    // Benefit text (e.g., "Up to 25% revenue share")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String benefit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private OrganizationPartnerProgram program;
}