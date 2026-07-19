package com.sharkdom.tablecustomization.entity.mypartner;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_my_partner_tables")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPartnerTable extends BaseEntity {

    private Long orgId;

    private String tableName;
}
