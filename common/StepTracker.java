package com.sharkdom.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Entity
@Builder
@Table(name = "t_step_tracker")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StepTracker extends BaseEntity {

    @JsonProperty("user_id")
    @Column(name = "user_id",unique = true)
    private String userId;

    @JsonProperty("step_one_completed")
    private Boolean stepOneCompleted;

    @JsonProperty("step_two_completed")
    private Boolean stepTwoCompleted;

    @JsonProperty("step_three_completed")
    private Boolean stepThreeCompleted;

    @JsonProperty("step_four_completed")
    private Boolean stepFourCompleted;

    @JsonProperty("step_five_completed")
    private Boolean stepFiveCompleted;

    @JsonProperty("step_six_completed")
    private Boolean stepSixCompleted;

    @JsonProperty("step_seven_completed")
    private Boolean stepSevenCompleted;

    @JsonProperty("step_eight_completed")
    private Boolean stepEightCompleted;

    @JsonProperty("step_nine_completed")
    private Boolean stepNineCompleted;

}
