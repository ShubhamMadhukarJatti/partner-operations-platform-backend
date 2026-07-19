package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Entity
@Table(name = "org_preferred_sub_sector")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PreferredSubSector extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    String area;

    public PreferredSubSector(String area) {
        this.area = area;
    }
}

