package com.sharkdom.dto;

import lombok.Data;

@Data
public class PartnerProgramDNSVerifyRequest {
    private String customDomain;
    private String targetHost;
    private String recordType;      // CNAME
    private String value;           // cname.vercel-dns.com OR generated host
    private String subdomainName;    // help
    private String domain;          // xyz.com
    private String formId;
}