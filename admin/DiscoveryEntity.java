package com.sharkdom.entity.admin;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organization_discovery")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class DiscoveryEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String organizationName;
    private String logoUrl;

}

