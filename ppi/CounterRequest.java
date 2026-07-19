package com.sharkdom.model.ppi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CounterRequest {

    private Boolean isClick;
    private Boolean isSubmit;
    private Long orgId;
    private String  userId;
    private String formId;
    private String formType;

}
