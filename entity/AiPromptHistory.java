package com.sharkdom.agenticai.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ai_prompt_history")
public class AiPromptHistory extends BaseEntity {

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "user_id", columnDefinition = "TEXT")
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "output_result")
    private Long outputResultId;

}