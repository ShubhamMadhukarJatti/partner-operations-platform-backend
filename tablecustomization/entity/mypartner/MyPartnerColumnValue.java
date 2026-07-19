package com.sharkdom.tablecustomization.entity.mypartner;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "t_my_partner_column_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"row_id", "column_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPartnerColumnValue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "row_id", nullable = false)
    private MyPartnerTableRow row;

    @ManyToOne
    @JoinColumn(name = "column_id", nullable = false)
    private MyPartnerTableColumn column;

    @Column(columnDefinition = "text")
    private String value;
}