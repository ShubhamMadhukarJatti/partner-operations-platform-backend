package com.sharkdom.model.ses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DomainVerificationResponse {
    private boolean success;
    private String domain;
    private String status;
    private DnsRecords dnsRecords;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DnsRecords {
        private DnsRecord verificationRecord;
        private List<DnsRecord> dkimRecords;
    }
}
