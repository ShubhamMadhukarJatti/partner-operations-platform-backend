package com.sharkdom.tablecustomization.entity.mypartner;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "t_my_partner_table_rows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"table_id", "source_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPartnerTableRow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private MyPartnerTable table;

    // Reference to myPartner
    @Column(name = "source_id", nullable = false, length = 64)
    private String sourceId;
}
