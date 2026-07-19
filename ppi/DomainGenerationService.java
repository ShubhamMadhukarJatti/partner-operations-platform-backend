package com.sharkdom.service.ppi;

import com.sharkdom.dto.GeneratedDomainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DomainGenerationService {

    @Value("${HOSTING_DOMAIN_SUFFIX}")
    private String hostingDomainSuffix;

    /**
     * Example:
     * input  -> help.xyz.com
     * output -> help + 30d6866646-hosting.sharkdom.com
     */
    public GeneratedDomainResponse generateTargetHost(String fullDomain) {
        String targetHost = hostingDomainSuffix;
        return new GeneratedDomainResponse(fullDomain, targetHost,"CNAME");
    }
}