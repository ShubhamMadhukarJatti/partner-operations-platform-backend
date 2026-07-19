package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class ScriptUpdateWrapper {

        private String scriptId;
    private String accessToken;
    private ScriptUpdateRequest scriptUpdateRequest;

}
