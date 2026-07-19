package com.sharkdom.controller.dns;


import com.sharkdom.model.email.DomainResponse;
import com.sharkdom.model.email.ForwardEmailRequestNew;
import com.sharkdom.model.email.TriggerTypeModel;
import com.sharkdom.model.ses.DomainVerificationResponse;
import com.sharkdom.service.email.AmazonSNS;
import com.sharkdom.service.email.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dns")
@Slf4j
public class DnsController {
    @Autowired
    private EmailService emailService;
//    private final AmazonSNS amazonSNS;


    @PostMapping("replay/emailToPartner")
    public String forwardEmail(@RequestBody ForwardEmailRequestNew request) throws Exception {
        return emailService.replyToOriginalSender(request);
    }
    @PostMapping("/sendToAllUser")
    public ResponseEntity<String> sendEmailToOrgUsers(@RequestBody ForwardEmailRequestNew request) throws Exception {
        String response = emailService.sendEmailToOrgUsers(request);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "")
    @PostMapping("/addDomain")
    public DomainVerificationResponse addDnsRecords(@RequestParam Long organizationId, @RequestParam String domain, @RequestParam String email) {
        return emailService.createDomainIdentity(organizationId, domain, email);
    }

    @Operation(summary = "")
    @GetMapping("/{domain}/status")
    public DomainVerificationResponse getStatus(@PathVariable String domain) {
        return emailService.getStatus(domain);
    }

    @Operation(summary = "")
    @GetMapping("/domain/{organizationId}")
    public DomainResponse getDetails(@PathVariable Long organizationId) {
        return emailService.getDomainDetails(organizationId);
    }
    // @Hidden
    @PostMapping("/trigger")
    public void triggerEmails(
            @RequestBody TriggerTypeModel triggerTypeModel) {
        if (triggerTypeModel.getScheduledAt() != null) {
            log.info("Scheduling email trigger for {}", triggerTypeModel);
            emailService.scheduleTask(triggerTypeModel);
        } else {
            emailService.sendTriggeredEmails(triggerTypeModel);
        }
    }

}
