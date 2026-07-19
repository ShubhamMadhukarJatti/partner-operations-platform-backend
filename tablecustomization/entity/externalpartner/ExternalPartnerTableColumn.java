package com.sharkdom.tablecustomization.entity.externalpartner;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.offlinePartner.entity.ColumnType;
import com.sharkdom.offlinePartner.entity.DynamicTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_external_partner_table_columns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPartnerTableColumn extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private ColumnType type;

    private Integer displayOrder;

    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private ExternalPartnerTable table;

}