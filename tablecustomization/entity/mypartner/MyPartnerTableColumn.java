package com.sharkdom.tablecustomization.entity.mypartner;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.offlinePartner.entity.ColumnType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_my_partner_table_columns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPartnerTableColumn extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private ColumnType type;

    private Integer displayOrder;

    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private MyPartnerTable table;

}