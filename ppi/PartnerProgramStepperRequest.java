package com.sharkdom.entity.ppi;

import lombok.Data;

@Data
public class PartnerProgramStepperRequest {

    private Boolean stepOneCompleted;
    private Boolean stepTwoCompleted;
    private Boolean stepThreeCompleted;
    private Boolean stepFourCompleted;
}
