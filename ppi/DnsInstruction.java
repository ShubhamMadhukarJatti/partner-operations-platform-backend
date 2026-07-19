package com.sharkdom.model.ppi;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DnsInstruction {
    private String type;
    private String name;
    private String value;
}