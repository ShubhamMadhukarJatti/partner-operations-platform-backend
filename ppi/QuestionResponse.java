package com.sharkdom.model.ppi;

import com.sharkdom.entity.ppi.InternalQuestion_Ppi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private String responseMessage;
    private Long formId;
    private InternalQuestion_Ppi savedQuestion;

    public QuestionResponse(String responseMessage) {
    }
}
