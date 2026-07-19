package com.sharkdom.model.ppi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleQuestionResponse {
    private Long questionId;
    private String responseTypePpi;
    private List<String> responseText;
    private List<OptionRequest> options;
}
