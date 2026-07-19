package com.sharkdom.entity.campaign;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "trigger_conditions")
public class Condition extends BaseEntity {
    private String conditionLabel;
    private Long templateId;
    private int delay;
    private int activeFor;
    private List<Integer> nodeIds;
    private String main;
    private String input;
    @ManyToOne
    @JoinColumn(name = "trigger_flow_id")
    @JsonBackReference
    private TriggerFlow triggerFlow;
}