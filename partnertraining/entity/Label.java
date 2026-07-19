package com.sharkdom.partnertraining.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "t_label")
@NoArgsConstructor
@AllArgsConstructor
public class Label extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private Long organizationId;

}