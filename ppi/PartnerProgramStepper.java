package com.sharkdom.entity.ppi;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_partner_program_stepper")
public class PartnerProgramStepper extends BaseEntity {

    private Long organizationId;
    private Boolean stepOneCompleted;
    private Boolean stepTwoCompleted;
    private Boolean stepThreeCompleted;
    private Boolean stepFourCompleted;
}
