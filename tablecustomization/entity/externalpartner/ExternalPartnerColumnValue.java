package com.sharkdom.tablecustomization.entity.externalpartner;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "t_External_partner_column_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"row_id", "column_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPartnerColumnValue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "row_id", nullable = false)
    private ExternalPartnerTableRow row;

    @ManyToOne
    @JoinColumn(name = "column_id", nullable = false)
    private ExternalPartnerTableColumn column;

    @Column(columnDefinition = "text")
    private String value;
}