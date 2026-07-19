package com.sharkdom.agenticai.entity;

import com.sharkdom.agenticai.enums.SearchStrictness;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "outreach_automation_settings")
@Getter
@Setter
public class OutreachAutomationSettings extends BaseEntity {

    private Long orgId;

    private String userId;

    private Integer dailyFrequency;

    @Enumerated(EnumType.STRING)
    private SearchStrictness searchStrictness;

    private Boolean linkedinActive;

    private Boolean emailActive;

}

