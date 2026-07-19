package com.sharkdom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DnsConnectionStatusResponse {

    private boolean dnsDataExists;
    private boolean cnameVerified;
    private boolean azureValidated;

}
