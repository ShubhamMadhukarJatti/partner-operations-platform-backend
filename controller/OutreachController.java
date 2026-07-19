package com.sharkdom.agenticai.controller;

import com.sharkdom.agenticai.entity.OutreachHistory;
import com.sharkdom.agenticai.model.*;
import com.sharkdom.agenticai.service.*;
import com.sharkdom.emailOutreach.dto.*;
import com.sharkdom.emailOutreach.service.MailgunServices;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/outreach")
@RequiredArgsConstructor
@Tag(name="Outreach API",description="AI outreach & automation APIs")
public class OutreachController {

    private final OutreachMessageService outreachService;
    private final OutreachEmailService outreachEmailService;
    private final EnterpriseLinkedinMessageService enterpriseLinkedinMessageService;
    private final EnterpriseLinkedinSignalService enterpriseLinkedinSignalService;
    private final OutreachHistoryService outreachHistoryService;
    private final MailgunServices mailgunServices;
    private final OutreachAutomationService outreachAutomationService;

    // ================= GENERATE MESSAGE =================
    @PostMapping("/generate-message")
    @Operation(summary="Generate Outreach Message",description="AI outreach generation")
    public SharkdomApiResponse<OutreachGenerateResponse> generateOutreach(@Valid @RequestBody OutreachGenerateRequest req){
        log.info("Generate outreach | orgId={} | orgName={} | channel={}",req.getORGid(),req.getORGname(),req.getChannel());
        OutreachGenerateResponse res=outreachService.generateOutreach(req);
        return new SharkdomApiResponse<>(true,"Outreach message generated",res);
    }

    // ================= GENERATE EMAIL =================
    @PostMapping("/generate-email")
    @Operation(summary="Generate Email Outreach")
    public SharkdomApiResponse<OutreachEmailResponse> generateEmailOutreach(@Valid @RequestBody OutreachEmailRequest req){
        log.info("Generate email | orgId={} | channel={}",req.getORGid(),req.getChannel());
        OutreachEmailResponse res=outreachEmailService.generateEmailOutreach(req);
        return new SharkdomApiResponse<>(true,"Outreach email generated",res);
    }

    // ================= SCHEMA =================
    @GetMapping("/schema")
    @Operation(summary="Fetch Outreach Schema")
    public SharkdomApiResponse<OutreachSchemaResponse> getSchema(){
        log.info("Fetch outreach schema");
        return new SharkdomApiResponse<>(true,"Schema fetched",outreachService.getOutreachSchema());
    }

    // ================= ENTERPRISE LINKEDIN =================
    @PostMapping("/enterprise-linkedin-message/generate")
    public SharkdomApiResponse<EnterpriseLinkedinMessageResponse> generateMessage(@Valid @RequestBody EnterpriseLinkedinMessageRequest req){
        log.info("Enterprise LinkedIn message | orgId={}",req.getOrgId());
        return new SharkdomApiResponse<>(true,"Enterprise LinkedIn message generated",
                enterpriseLinkedinMessageService.generateEnterpriseLinkedinMessage(req));
    }

    // ================= INSUFFICIENT SIGNAL =================
    @PostMapping("/insufficient-signal")
    public SharkdomApiResponse<EnterpriseLinkedinInsufficientSignalResponse> insufficientSignal(@RequestBody EnterpriseLinkedinInsufficientSignalRequest req){
        log.info("Insufficient signal | orgId={}",req.getOrgId());
        return new SharkdomApiResponse<>(true,"AI guardrail triggered",
                enterpriseLinkedinSignalService.handleInsufficientSignal(req));
    }

    // ================= HISTORY =================
    @GetMapping("/history/data")
    public SharkdomApiResponse<List<OutreachHistory>> getOutreachHistory(){
        Long orgId=Util.getOrgIdFromToken();
        log.info("Fetch outreach history | orgId={}",orgId);
        List<OutreachHistory> res=outreachHistoryService.getOutreachHistoryByOrgId(orgId);
        return new SharkdomApiResponse<>(true,"History fetched",res);
    }

    // ================= SUMMARY =================
    @GetMapping("/transaction-summary")
    public SharkdomApiResponse<OutreachTransactionSummaryResponse> getTransactionSummary(){
        Long orgId=Util.getOrgIdFromToken();
        log.info("Fetch transaction summary | orgId={}",orgId);
        return new SharkdomApiResponse<>(true,"Summary fetched",outreachHistoryService.getTransactionSummary(orgId));
    }

    // ================= SEND EMAIL =================
    @PostMapping("/send-email")
    public SharkdomApiResponse<SendMailResponse> sendEmail(@Valid @RequestBody SendMailRequest req){
        log.info("Send email | to={} | subject={}",req.getTo(),req.getSubject());
        SendMailResponse res=mailgunServices.sendEmail(req);
        return new SharkdomApiResponse<>(true,"Email sent",res);
    }

    // ================= SETTINGS =================
    @PostMapping("/settings")
    public SharkdomApiResponse<OutreachAutomationResponse> saveSettings(@RequestBody OutreachAutomationRequest req){
        Long orgId=Util.getOrgIdFromToken();
        OutreachAutomationResponse res=outreachAutomationService.saveSettings(req,orgId,req.getUserId());
        return new SharkdomApiResponse<>(true,"Settings saved",res);
    }

    // ================= SAVE HISTORY =================
    @Operation(summary="Save Outreach History",description="Save outreach record")
    @PostMapping("/save")
    public SharkdomApiResponse<OutreachHistoryResponse> saveHistory(@RequestBody OutreachHistorySaveRequest req){

        OutreachHistory saved=outreachHistoryService.saveOutreachHistory(
                req.getCompanyName(),req.getRecipientName(),req.getRecipientTitle(),
                req.getChannel(),req.getStatus(),req.getUserId(),req.getOrgId());

        OutreachHistoryResponse res=new OutreachHistoryResponse();
        res.setId(saved.getId()); res.setCompanyName(saved.getCompanyName());
        res.setRecipientName(saved.getRecipientName()); res.setRecipientTitle(saved.getRecipientTitle());
        res.setChannel(saved.getChannel()); res.setStatus(saved.getStatus());
        res.setUserId(saved.getUserId()); res.setOrgId(saved.getOrgId()); res.setSentAt(saved.getSentAt());

        return new SharkdomApiResponse<>(true,"History saved",res);
    }
}