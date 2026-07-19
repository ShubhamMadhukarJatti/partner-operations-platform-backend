package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_director")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrganizationDirector extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String dinOrPanNumber;
}