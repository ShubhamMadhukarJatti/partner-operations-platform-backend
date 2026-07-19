package com.sharkdom.model.email;

import com.sharkdom.model.ses.DomainVerificationResponse;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DomainResponse {
    private Long organizationId;
    private String domain;
    private String email;
    private DomainVerificationResponse.DnsRecords dnsRecords;
    private boolean isVerified;
    private boolean isEmailVerified;

}
