package com.sharkdom.model.ppi;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkSaveResponseRequest {
    private Long formId;
    private Long orgId;
    private String userId;
    private List<SingleQuestionResponse> responses;
}

