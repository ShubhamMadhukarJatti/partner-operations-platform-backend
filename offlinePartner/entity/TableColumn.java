package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "table_columns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableColumn extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private ColumnType type;

    private Integer displayOrder;

    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private DynamicTable table;
}