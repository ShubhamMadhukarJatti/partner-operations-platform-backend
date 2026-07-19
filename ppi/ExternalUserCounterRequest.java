package com.sharkdom.model.ppi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalUserCounterRequest {
    private Boolean isClick;
    private Boolean isSubmit;
    private String formId;
    private String formType;
}
