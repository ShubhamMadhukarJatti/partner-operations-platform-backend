package com.sharkdom.controller.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.entity.email.EmailStatistics;
import com.sharkdom.entity.email.EmailSubscribed;
import com.sharkdom.model.email.*;
import com.sharkdom.model.ses.DomainVerificationResponse;
import com.sharkdom.service.email.AmazonSNS;
import com.sharkdom.service.email.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {
    @Autowired
    private EmailService emailService;
    private final AmazonSNS amazonSNS;

    public EmailController(AmazonSNS amazonSNS) {
        this.amazonSNS = amazonSNS;
    }

    @Operation(summary = "Send one email to multiple recipients without multipart attachments, it supports attachments from S3 bucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully sent emails.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EmailReqModel.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/sendOne")
    public EmailReqModelWithResponse sendOne(@RequestBody EmailReqModel emailReqModel) {
        return emailService.sendOne(new EmailReqModelWithMultipartAttachments(emailReqModel));
    }

    @Operation(summary = "Verify update email")
    @PostMapping("/verifyUpdateEmail")
    public Map<String, String> verifyUpdateEmail(@RequestParam String originalEmail, @RequestParam String otp) {
        return emailService.verifyUpdateEmail(originalEmail, otp);
    }

    @Operation(summary = "Send email update")
    @PostMapping("/sendEmailUpdate")
    public Map<String, String> sendEmailUpdate(@RequestBody EmailUpdateRequest emailReqModel) throws ExecutionException, InterruptedException {
        return emailService.sendEmailUpdate(emailReqModel);
    }

    @Operation(summary = "Send list of emails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully sent emails.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EmailReqModelWithMultipartAttachments.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/sendMany")
    public List<EmailReqModelWithResponse> sendMultiple(@RequestBody List<EmailReqModelWithMultipartAttachments> emails) {
        return emailService.sendMultiple(emails, "templateCode", 1L, 1L);
    }

    @Operation(summary = "Send list of emails for provided UserId and template code, s3AttachmentNames is optional, if you don't provide then it will use the one present in template")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully sent emails.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = TemplateEmailReqModel.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/sendUsingTemplate")
    public List<EmailReqModelWithResponse> sendByTemplateAndUserIds(
            @RequestBody TemplateEmailReqModel templateEmailReqModel) {
        return emailService.sendByTemplateAndUserIds(templateEmailReqModel, null);
    }

    @Operation(summary = "Send list of emails for provided OrganizationIds and template code, s3AttachmentNames is optional, if you don't provide then it will use the one present in template")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully sent emails.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = TemplateEmailReqModel.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/sendUsingTemplate/org")
    public List<EmailReqModelWithResponse> sendByTemplateAndOrganizationIds(
            @RequestBody TemplateOrganizationEmailReqModel templateOrganizationEmailReqModel) {
        return emailService.sendByTemplateAndOrganizationIds(templateOrganizationEmailReqModel, null, 1L, 1L);
    }

    @Operation(summary = "Send one email with attachment multipart files. " +
            "##How to send mail with attachments from POSTMAN---\n" +
            "\n" +
            "Add below as formdata with key\n" +
            "{\n" +
            "    \"s3AttachmentNames\":[\"HRMS.pdf\"],\n" +
            "    \"recipients\":[\"choudharysatish10@gmail.com\"],\n" +
            "    \"bodyHtml\":\"<html>/<html>\",\n" +
            "    \"subject\":\"Mail 4 Send From S3 Bucket AWS Production\",\n" +
            "    \"sender\":\"support@sharkdom.com\"\n" +
            "}\n" +
            "\n" +
            "Add another formdata with key attachmentList and choose type as File and then select files to upload\n" +
            "\n" +
            "##Note s3AttachmentNames is the list of files that you want to attach from S3 bucket, the bucket name is present in fireStore configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully sent email.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EmailReqModel.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/sendOneWithAttachment", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public EmailReqModelWithResponse sendOneWithAttachment(@RequestPart String emailRequest, @RequestPart List<MultipartFile> attachmentList) throws JsonProcessingException {
        EmailReqModel emailReqModel = new ObjectMapper().readValue(emailRequest, EmailReqModel.class);
        return emailService.sendOne(emailReqModel.withMultipartAttachments(attachmentList));
    }

    @Operation(summary = "Trigger emails with triggerType and templateCode." +
            "Possible values for triggerType = NOT_MIGRATED, USER_ONE_TIME, NOT_KYB, PROFILE_NOT_COMPLETED, ORG_ONE_TIME, EMAIL_SUBSCRIBED, UNVERIFIED_EMAIL_ORG")
    @PostMapping("/trigger")
    public void triggerEmails(
            @RequestBody TriggerTypeModel triggerTypeModel) {
        if (triggerTypeModel.getScheduledAt() != null) {
            log.info("Scheduling email trigger for {}", triggerTypeModel);
            emailService.scheduleTask(triggerTypeModel);
        } else {
            log.info("Triggering emails immediately for {}", triggerTypeModel);
            emailService.sendTriggeredEmails(triggerTypeModel);
        }
    }


    @PostMapping("/sns")
    public ResponseEntity<String> snsData(@RequestHeader Map<String, String> headers, @RequestBody String data) {

        amazonSNS.saveSNSData(data);
        return ResponseEntity.ok("saved");

    }

    @Operation(summary = "Get stats of email campaign by eventType, env, templateCode, sentAt")
    @GetMapping("/statistics")
    public ResponseEntity<Page<EmailStatistics>> getEmailStatistics(@Schema(allowableValues = {"Open", "Click", "Bounce"}) @RequestParam(required = false) String eventType,
                                                                    @Schema(allowableValues = {"DEV", "PROD"}) @RequestParam(required = false, defaultValue = "DEV") String env,
                                                                    @RequestParam(required = false) String templateCode,
                                                                    @Schema(defaultValue = "2024-04-12") @RequestParam(required = false) String sentAt,
                                                                    @RequestParam(defaultValue = "20") int size,
                                                                    @RequestParam(defaultValue = "0") int page) {
        var response = amazonSNS.getEmailStatistics(eventType, env, templateCode, sentAt, size, page);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Verify Email")
    @PostMapping("/verify")
    public ResponseEntity<EmailVerify> verifyEmail(@RequestParam String transactionId, @RequestParam String code, @RequestParam(required = false) String referralCode) {
        return emailService.verifyEmail(transactionId, code, referralCode);
    }

    @Operation(summary = "Resend verification email to all organization users")
    @PostMapping("/resend")
    public ResponseEntity<Void> resendVerificationEmail(@RequestParam Long organizationId) {
        emailService.resendVerificationEmail(organizationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Reset Password Email")
    @PostMapping("/resetPassword")
    public List<EmailReqModelWithResponse> resetPasswordEmail(@RequestParam String email, @RequestParam String link) {
        return emailService.resetPassword(email, link);
    }


    @Operation(summary = "")
    @PostMapping("/subscribe")
    public EmailSubscribed subscribe(@RequestParam String email) {
        return emailService.subscribe(email);
    }

    @Operation(summary = "Get All Subscribed Email")
    @GetMapping("/subscribe")
    public List<EmailSubscribed> subscribed() {
        return emailService.getAllSubscribedEmails();
    }

/*    @Operation(summary = "")
    @PostMapping("/addDomain")
    public DomainVerificationResponse addDnsRecords(@RequestParam Long organizationId, @RequestParam String domain, @RequestParam String email) {
        return emailService.createDomainIdentity(organizationId, domain, email);
    }*/


    @Operation(summary = "Get all campaign stats")
    @GetMapping("/campaign/statistics")
    public ResponseEntity<Page<AllCampaignStats>> getEmailStatistics(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<AllCampaignStats> allCampaignStatistics = amazonSNS.getAllCampaignStatistics(page, size);
        return ResponseEntity.ok(allCampaignStatistics);
    }

    @Operation(summary = "Invite organization for discord")
    @PostMapping("/discord/invite")
    public void inviteDiscord(@RequestParam Long partnerOrganizationId, @RequestParam String discordLink) throws Exception {
      emailService.inviteDiscord(partnerOrganizationId, discordLink);
    }

}
