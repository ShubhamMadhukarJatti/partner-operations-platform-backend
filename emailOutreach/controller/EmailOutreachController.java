package com.sharkdom.emailOutreach.controller;

import com.sharkdom.emailOutreach.dto.*;
import com.sharkdom.emailOutreach.entity.Email;
import com.sharkdom.emailOutreach.entity.EmailAccount;
import com.sharkdom.emailOutreach.service.EmailOutreachService;
import com.sharkdom.emailOutreach.service.MailgunServices;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/email/outreach")
public class EmailOutreachController {

    @Autowired
    private EmailOutreachService emailOutreachService;

    @Autowired
    private MailgunServices mailgunServices;

    @PostMapping("/mailbox/claim")
    public ResponseEntity<EmailAccount> claimEmail() {
        log.info("Request received to claim or create email account");
        EmailAccount account = emailOutreachService.claimOrCreateEmail();
        return ResponseEntity.ok(account);
    }

    @GetMapping("/mailbox/claim/check")
    public ResponseEntity<EmailBoxClaimCheckResponse> getEmailAccount() {
        log.info("Request received to get claimed email account");
        EmailAccount account = emailOutreachService.getEmailAccount();
        boolean exists = (account != null);
        EmailBoxClaimCheckResponse response = new EmailBoxClaimCheckResponse(exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/message/send")
    public ResponseEntity<SendMailResponse> sendMail(@RequestBody SendMailRequest sendMailRequest) {
        log.info("Request received to send email: {}", sendMailRequest);
        SendMailResponse response = emailOutreachService.sendEmail(sendMailRequest);
        if ("error".equalsIgnoreCase(response.getId())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("message/event/counts")
    public ResponseEntity<OrgMessageEventSummary> getSummary() {
        log.info("Request received to get organization message event summary");
        return ResponseEntity.ok(emailOutreachService.getOrgMessageEventSummary());
    }

    @GetMapping("/message/summary/details")
    public ResponseEntity<MessageEventSummary> getMessageEventSummary(
            @RequestParam String messageId) {
        log.info("Received request for event summary, messageId={}", messageId);
        MessageEventSummary summary = emailOutreachService.getMessageEventSummaryById(messageId);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/message/event/summary")
    public ResponseEntity<OrgMessageEventSummary> getOrgMessageEventSummaryByEmails(
            @RequestBody List<Long> partnerIds) {
        log.info("Received request to get message event summary for partners: {}", partnerIds);
        OrgMessageEventSummary summary = emailOutreachService.getOrgMessageEventSummaryByEmails(partnerIds);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/message/event/summary/external/partner/details")
    public ResponseEntity<OrgMessageEventSummary> getOrgMessageEventSummaryByEmailsForExternalPartnerDetails(
            @RequestParam String externalPartnerCode) {
        log.info("Received request to get message event summary for external partner: {}", externalPartnerCode);
        OrgMessageEventSummary summary = emailOutreachService.getOrgMessageEventSummaryByEmailsForExternalPartner(externalPartnerCode);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/message/event/summary/external/partner")
    public ResponseEntity<OrgMessageEventSummary> getOrgMessageEventSummaryByEmailsForExternalPartner(
            ) {
        log.info("Received request to get message event summary for external partner");
        OrgMessageEventSummary summary = emailOutreachService.getOrgMessageEventSummaryBasedOnOrgId();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/message/sends")
    public ResponseEntity<List<Email>> getTotalEmailsSent() {
        log.info("Request received to get total emails sent");
        List<Email> sendEmails = emailOutreachService.getSendEmails();
        return ResponseEntity.ok(sendEmails);
    }

    @GetMapping("/message/receives")
    public ResponseEntity<List<Email>> getTotalEmailsReceived() {
        log.info("Request received to get total emails received");
        List<Email> receivedEmails = emailOutreachService.getReceivedEmails();
        return ResponseEntity.ok(receivedEmails);
    }

    @PostMapping("/message/send/external/partner")
    public ResponseEntity<SendMailResponse> sendMailExternalPartner(@RequestBody SendMailRequest sendMailRequest) {
        log.info("Request received to send email: {}", sendMailRequest);
        SendMailResponse response = emailOutreachService.sendEmailExternalPartner(sendMailRequest);
        if ("error".equalsIgnoreCase(response.getId())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    public ResponseEntity<SendMailResponse> sendEmail(
            @Valid @RequestBody SendMailRequest request
    ) {
        SendMailResponse response = mailgunServices.sendEmail(request);

        if ("error".equalsIgnoreCase(response.getId())) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}
