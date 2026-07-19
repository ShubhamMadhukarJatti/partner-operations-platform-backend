package com.sharkdom.tablecustomization.entity.overlaprecordfieldentity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableRow;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "t_overlap_record_field_entity_column_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"row_id", "column_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverlapRecordFieldEntityColumnValue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "row_id", nullable = false)
    private OverlapRecordFieldEntityRow row;

    @ManyToOne
    @JoinColumn(name = "column_id", nullable = false)
    private OverlapRecordFieldEntityTableColumn column;

    @Column(columnDefinition = "text")
    private String value;
}