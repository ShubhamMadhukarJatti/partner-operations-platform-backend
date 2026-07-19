package com.sharkdom.tablecustomization.entity.overlaprecordfieldentity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "t_overlap_record_field_entity_row",
        uniqueConstraints = @UniqueConstraint(columnNames = {"table_id", "source_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverlapRecordFieldEntityRow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private OverlapRecordFieldEntityTable table;

    // Reference to OfflinePartnerInvite.id
    @Column(name = "source_id", nullable = false, length = 64)
    private String sourceId;
}
