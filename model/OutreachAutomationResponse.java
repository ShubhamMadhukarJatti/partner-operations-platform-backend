package com.sharkdom.agenticai.model;

import com.sharkdom.agenticai.enums.SearchStrictness;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutreachAutomationResponse {

    private Integer dailyFrequency;

    private SearchStrictness searchStrictness;

    private Boolean linkedinActive;

    private Boolean emailActive;

}

