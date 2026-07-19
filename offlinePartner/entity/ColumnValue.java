package com.sharkdom.offlinePartner.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "column_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"row_id", "column_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnValue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "row_id", nullable = false)
    private TableRow row;

    @ManyToOne
    @JoinColumn(name = "column_id", nullable = false)
    private TableColumn column;

    @Column(columnDefinition = "text")
    private String value;
}

