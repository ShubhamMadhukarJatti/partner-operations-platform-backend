package com.sharkdom.offlinePartner.entity;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "table_rows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableRow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private DynamicTable table;
}
