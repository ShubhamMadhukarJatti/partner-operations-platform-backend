package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_organization_certifications_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationCertificationsConfig extends BaseEntity {

    @Column(name = "certification_name", nullable = false)
    private String certificationName;

    @Column(name = "logo_url", nullable = false)
    private String logoUrl;
}