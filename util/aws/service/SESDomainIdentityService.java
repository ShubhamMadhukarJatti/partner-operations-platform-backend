package com.sharkdom.util.aws.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.ses.DnsRecord;
import com.sharkdom.model.ses.DomainVerificationResponse;
import com.sharkdom.util.aws.config.AwsConfigsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SESDomainIdentityService {


    public DomainVerificationResponse createDomainIdentity(String domain) {
        try {
            // Create domain identity
            final AmazonSimpleEmailService sesClient = getSESClient();
            VerifyDomainIdentityResult verifyResult = sesClient.verifyDomainIdentity(
                    new VerifyDomainIdentityRequest().withDomain(domain)
            );

            // Verify DKIM for the domain
            VerifyDomainDkimResult dkimVerifyResult = sesClient.verifyDomainDkim(
                    new VerifyDomainDkimRequest().withDomain(domain)
            );

            // Get DKIM tokens
            GetIdentityDkimAttributesResult dkimResult = sesClient.getIdentityDkimAttributes(
                    new GetIdentityDkimAttributesRequest().withIdentities(domain)
            );

            // Get verification status
            GetIdentityVerificationAttributesResult verificationResult =
                    sesClient.getIdentityVerificationAttributes(
                            new GetIdentityVerificationAttributesRequest().withIdentities(domain)
                    );

            // Create DNS records
            List<String> dkimTokens = dkimVerifyResult.getDkimTokens();
            List<DnsRecord> dkimRecords = dkimTokens.stream()
                    .map(token -> DnsRecord.builder()
                            .type("CNAME")
                            .name(token + "._domainkey." + domain)
                            .value(token + ".dkim.amazonses.com")
                            .ttl(600)
                            .build())
                    .collect(Collectors.toList());

            DnsRecord verificationRecord = DnsRecord.builder()
                    .type("TXT")
                    .name("_amazonses." + domain)
                    .value(verifyResult.getVerificationToken())
                    .ttl(600)
                    .build();

            String status = verificationResult.getVerificationAttributes()
                    .get(domain).getVerificationStatus();

            return DomainVerificationResponse.builder()
                    .success(true)
                    .domain(domain)
                    .status(status)
                    .dnsRecords(DomainVerificationResponse.DnsRecords.builder()
                            .verificationRecord(verificationRecord)
                            .dkimRecords(dkimRecords)
                            .build())
                    .build();

        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH137, e.getMessage(), e);
        }
    }


        public DomainVerificationResponse getDomainStatus(String domain) {
            try {
                log.info("Fetching SES domain status for: {}", domain);

                final AmazonSimpleEmailService sesClient = getSESClient();

                // Get verification status
                GetIdentityVerificationAttributesResult verificationResult =
                        sesClient.getIdentityVerificationAttributes(
                                new GetIdentityVerificationAttributesRequest().withIdentities(domain)
                        );
                log.debug("SES verification result for {}: {}", domain, verificationResult);

                // Get DKIM status
                GetIdentityDkimAttributesResult dkimResult =
                        sesClient.getIdentityDkimAttributes(
                                new GetIdentityDkimAttributesRequest().withIdentities(domain)
                        );
                log.debug("SES DKIM result for {}: {}", domain, dkimResult);

                // Safely extract verification status
                String verificationStatus = "NotFound";
                if (verificationResult.getVerificationAttributes() != null &&
                        verificationResult.getVerificationAttributes().containsKey(domain) &&
                        verificationResult.getVerificationAttributes().get(domain) != null) {
                    verificationStatus = verificationResult.getVerificationAttributes()
                            .get(domain).getVerificationStatus();
                }

                // Safely extract DKIM status
                String dkimStatus = "NotFound";
                if (dkimResult.getDkimAttributes() != null &&
                        dkimResult.getDkimAttributes().containsKey(domain) &&
                        dkimResult.getDkimAttributes().get(domain) != null) {
                    dkimStatus = dkimResult.getDkimAttributes()
                            .get(domain).getDkimVerificationStatus();
                }

                log.info("Domain status for {} → Verification: {}, DKIM: {}", domain, verificationStatus, dkimStatus);

                return DomainVerificationResponse.builder()
                        .success(true)
                        .domain(domain)
                        .status(String.format("Domain Verification: %s, DKIM: %s",
                                verificationStatus, dkimStatus))
                        .build();

            } catch (Exception e) {
                log.error("Error fetching domain status for {}: {}", domain, e.getMessage(), e);
                throw new ServiceException(
                        ErrorMessages.SH116,
                        "Failed to fetch domain status for " + domain + ": " + e.getMessage(),
                        e
                );
            }

    }



    private AmazonSimpleEmailService getSESClient() {
        try {
            return AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(
                                    AwsConfigsProvider.getSesConfigs().get("accessKey"),
                                    AwsConfigsProvider.getSesConfigs().get("secretKey"))))
                    .withRegion(Regions.AP_SOUTH_1)
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException(ErrorMessages.SH116, e.getMessage(), e);
        }
    }


}
