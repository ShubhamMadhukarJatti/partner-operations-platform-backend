package com.sharkdom.model.ppi;

import lombok.Data;

import java.util.List;

@Data
public class WebHookQuestionResponse {

    private String question;
    private List<String> response;
    private String formId;
    private String formName;
}
