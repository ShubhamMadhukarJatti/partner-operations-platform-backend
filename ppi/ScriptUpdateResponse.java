package com.sharkdom.model.ppi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptUpdateResponse {
    private String scriptId;
    private List<ScriptFile> files;



    // getters/setters for scriptId, files...
}
