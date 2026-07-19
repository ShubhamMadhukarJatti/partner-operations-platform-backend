package com.sharkdom.model.campaign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TriggerTemplateRequest {
    private String body;
    private String subject;
    private String userId;
}
