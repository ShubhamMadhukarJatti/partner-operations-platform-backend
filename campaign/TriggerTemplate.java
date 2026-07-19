package com.sharkdom.entity.campaign;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trigger_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerTemplate extends BaseEntity {
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    private String subject;
    private String userId;
}
