package com.sharkdom.agenticai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPromptHistoryRequest {

    private Long orgId;

    private String userId;

    private String prompt;

    private Long outputResultId;

}
