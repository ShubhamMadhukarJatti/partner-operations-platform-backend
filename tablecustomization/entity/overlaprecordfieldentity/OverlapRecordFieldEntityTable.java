package com.sharkdom.tablecustomization.entity.overlaprecordfieldentity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_overlap_record_field_entity_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverlapRecordFieldEntityTable extends BaseEntity {

    private Long orgId;

    private String tableName;
}
