package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class FormLinkCheckResponse {
    private boolean linked;
    private String sheetId;
}
