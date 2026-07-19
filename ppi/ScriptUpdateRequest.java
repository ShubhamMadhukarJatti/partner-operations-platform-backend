package com.sharkdom.model.ppi;


import lombok.Data;

import java.util.List;
@Data

public class ScriptUpdateRequest {

    private List<ScriptFile> files;
}


