package com.sharkdom.model.ses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DnsRecord {
    private String type;
    private String name;
    private String value;
    private int ttl;
}