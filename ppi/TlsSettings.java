package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class TlsSettings {
    private String certificateType;
    private String minimumTlsVersion;
    private String secret;
}
