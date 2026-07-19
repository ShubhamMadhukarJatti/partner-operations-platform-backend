package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.constants.BenefitsStatus;
import com.sharkdom.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "sendor_benefit")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SenderBenefit extends BaseEntity {

    private static final long serialVersionUID = 1L;
    String benefit;
    String description;
    @Schema(defaultValue = "false")
    boolean activeConversation = false;
    private BenefitsStatus status = BenefitsStatus.ACTIVE;
}
