package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "partnership_benefit")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PartnershipBenefit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    String benefit;
    String description;
}
