package com.sharkdom.entity.ppi;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="formApplyStatus")
@Builder
public class FormApplyStatus extends BaseEntity {

    private Long formId;
    private Long appliedOrgId;
    private Boolean isApplied;

    private Timestamp appliedOn;


    @ManyToOne
    @JoinColumn(name = "applicant_org_id")
    private Organization applicantOrg;

    // Org that owns the form (org 123)
    @ManyToOne
    @JoinColumn(name = "form_owner_org_id")
    private Organization formOwnerOrg;
}
