package com.sharkdom.partnerattribution.entities;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnerattribution.enums.ContactSource;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_shared_contacts")
@Data
public class SharedContact extends BaseEntity {

    private Long orgId;

    private Long partnerOrgId;

    private String dealId;

    private String name;
    private String title;

    @Enumerated(EnumType.STRING)
    private ContactSource source;

    private String relationship;

    private Boolean inCrm = false;

    private Boolean isDeleted = false;
}