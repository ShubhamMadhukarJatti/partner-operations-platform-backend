package com.sharkdom.tablecustomization.entity.externalpartner;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_external_partner_tables")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPartnerTable extends BaseEntity {

    private Long orgId;

    private String tableName;
}
