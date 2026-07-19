package com.sharkdom.controller.campaign;

import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.entity.campaign.TriggerTemplate;
import com.sharkdom.model.campaign.*;
import com.sharkdom.service.campaign.TriggerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class TriggerController {
    private final TriggerService triggerService;

    @PostMapping("/trigger/general")
    public ResponseEntity<GeneralTriggerBaseResponse> createGeneralTrigger(@RequestBody @Valid GeneralTriggerRequest triggerRequest) {
        return triggerService.createGeneralTrigger(triggerRequest);
    }

    @PatchMapping("/updateTrigger")
    public ResponseEntity<Map<String, String>> updateGeneralTrigger(@RequestBody TriggerUpdateRequest triggerUpdateRequest) {
        return ResponseEntity.ok(triggerService.updateTrigger(triggerUpdateRequest));
    }

    @GetMapping("/triggers")
    public ResponseEntity<List<GeneralTriggerBaseResponse>> getTriggersByOrgId(@RequestParam long organizationId) {
        return ResponseEntity.ok(triggerService.getTriggersByOrgId(organizationId));
    }

    @GetMapping("/triggers/{id}")
    public ResponseEntity<GeneralTriggerResponse> getTriggerById(@PathVariable("id") long triggerId) {
        return ResponseEntity.ok(triggerService.getTriggerById(triggerId));
    }

    @PostMapping("/trigger/template")
    public ResponseEntity<Map<String, Long>> createTriggerTemplate(@RequestBody TriggerTemplateRequest templateRequest) {
        return new ResponseEntity<>(triggerService.createTriggerTemplate(templateRequest), HttpStatus.CREATED);
    }

    @GetMapping("/trigger/template")
    public ResponseEntity<List<TriggerTemplate>> getTriggerTemplates(@RequestParam String userId) {
        return ResponseEntity.ok(triggerService.getTriggerTemplates(userId));
    }

    @GetMapping("/trigger/template/{id}")
    public ResponseEntity<Optional<TriggerTemplate>> getTriggerTemplate(@PathVariable("id") long templateId) {
        return ResponseEntity.ok(triggerService.getTriggerTemplateById(templateId));
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> saveCampaign(@RequestParam Long organizationId, @RequestBody Campaign campaign) {
        return ResponseEntity.ok(triggerService.saveCampaign(organizationId, campaign));
    }

    @GetMapping
    public ResponseEntity<CampaignResponse> getCampaign(@RequestParam Long organizationId, @RequestParam CampaignType campaignType) {
        return ResponseEntity.ok(triggerService.getCampaign(organizationId, campaignType));
    }
}
