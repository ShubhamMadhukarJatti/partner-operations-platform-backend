package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_preferred_sector")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PreferredSector extends BaseEntity {
    private static final long serialVersionUID = 1L;
    String area;

    public PreferredSector(String area) {
        this.area = area;
    }
}

