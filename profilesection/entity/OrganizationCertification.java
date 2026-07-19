package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.profilesection.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_organization_certifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCertification extends BaseEntity {


    // Multi-tenant support
    @Column(nullable = false)
    private Long organizationId;

    // Certification Name (e.g., AWS Certified, Google Partner)
    @Column(nullable = false)
    private String certificationName;

    // Verification URL
    @Column(length = 1000)
    private String verificationUrl;

    // Status (PENDING, VERIFIED, REJECTED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    // When user submits for review
    private LocalDateTime submittedAt;

    // When admin verifies
    private LocalDateTime verifiedAt;

    // Admin remarks (optional)
    @Column(length = 1000)
    private String remarks;

    private Long certificateId;

}