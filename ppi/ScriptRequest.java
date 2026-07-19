package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class ScriptRequest {

    private String sheetId;
    private String formId;
    private String scriptId;
    private String formName;
    private String sheetName;
    private String bearerToken;
//    private String scriptUrl;
}
