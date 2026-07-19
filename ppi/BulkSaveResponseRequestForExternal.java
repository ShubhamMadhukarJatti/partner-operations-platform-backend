package com.sharkdom.model.ppi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkSaveResponseRequestForExternal {
    private Long formId;
    private String brandName;
    private String email;
    private String username;
    private boolean isExternalSubmission;
    private List<SingleQuestionResponse> responses;
}

