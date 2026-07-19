package com.sharkdom.tablecustomization.entity.overlaprecordfieldentity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.offlinePartner.entity.ColumnType;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_overlap_record_field_entity_table_column")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverlapRecordFieldEntityTableColumn extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private ColumnType type;

    private Integer displayOrder;

    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private OverlapRecordFieldEntityTable table;

}