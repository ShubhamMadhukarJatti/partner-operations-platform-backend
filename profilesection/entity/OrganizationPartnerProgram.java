package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_organization_partner_program")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationPartnerProgram extends BaseEntity {


    // Multi-tenant support
    @Column(nullable = false, unique = true)
    private Long organizationId;

    // Program Name
    @Column(nullable = false)
    private String programName;

    // Toggle (Enable/Disable)
    @Column(nullable = false)
    private Boolean isActive;

    // Optional URL
    @Column(length = 1000)
    private String programUrl;

    @Builder.Default
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationPartnerProgramBenefit> benefits = new ArrayList<>();

}