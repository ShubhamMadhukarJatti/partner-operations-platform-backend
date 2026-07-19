package com.sharkdom.model.ppi;

import com.sharkdom.constants.ppi.FormOperation;
import lombok.Data;

@Data
public class FormEnableRequest {
    private String formName;
    private String responderUrl;
    private FormOperation operation;
    private String formType;
    private String formId;
    private Boolean isFormEnabled;


}
