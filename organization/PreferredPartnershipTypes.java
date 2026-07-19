package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_preferred_partnership_types")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PreferredPartnershipTypes extends BaseEntity {

    private static final long serialVersionUID = 1L;
    private String area;

    public PreferredPartnershipTypes(String area) {
        this.area = area;
    }
}
