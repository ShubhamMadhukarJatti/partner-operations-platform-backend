package com.sharkdom.model.ppi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScriptFile {

    private String name;
    private String type;
    private String source;
    private LastModifyUser lastModifyUser;
    private String createTime;
    private String updateTime;
    private FunctionSet functionSet;
}
