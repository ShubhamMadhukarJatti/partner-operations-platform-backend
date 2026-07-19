package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dynamic_tables")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicTable extends BaseEntity {

    private Long orgId;

    private String email;

    private String name;
}