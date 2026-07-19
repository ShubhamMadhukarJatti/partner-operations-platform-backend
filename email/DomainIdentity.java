package com.sharkdom.entity.email;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "domain_identity")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DomainIdentity extends BaseEntity {
    private Long organizationId;
    private String domain;
    private String email;
    @Column(columnDefinition = "LONGTEXT")
    private String dnsRecords;
    private boolean isVerified;
    private boolean isEmailVerified;
}
