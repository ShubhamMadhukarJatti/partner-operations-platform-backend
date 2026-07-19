package com.sharkdom.service.campaign;

import com.google.gson.Gson;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.constants.campaign.TriggerStatus;
import com.sharkdom.entity.campaign.*;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.campaign.*;
import com.sharkdom.repository.campaign.GeneralTriggerRepository;
import com.sharkdom.repository.campaign.TriggerTemplateRepository;
import com.sharkdom.repository.integration.CampaignIntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.service.email.AmazonSes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TriggerService {
    private final AmazonSes amazonSes;
    private final GeneralTriggerRepository generalTriggerRepository;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final OrganizationRepository organizationRepository;
    private final TriggerTemplateRepository triggerTemplateRepository;
    private final CampaignIntegrationRepository campaignIntegrationRepository;
    @Value("${email.default-sender}")
    private String defaultSender;

    public ResponseEntity<GeneralTriggerBaseResponse> createGeneralTrigger(GeneralTriggerRequest triggerRequest) {
        var triggerFlow = TriggerFlow.builder()
                .conditions(triggerRequest.getTriggerFlow().getConditions())
                .edges(triggerRequest.getTriggerFlow().getEdges())
                .nodes(triggerRequest.getTriggerFlow().getNodes())
                .build();
        var triggerRe = GeneralTrigger.builder()
                .organizationId(triggerRequest.getOrganizationId())
                .sendAll(triggerRequest.isSendAll())
                .partnerIds(triggerRequest.getPartnerIds())
                .status(triggerRequest.getStatus())
                .campaignName(triggerRequest.getCampaignName())
                .campaignType(triggerRequest.getCampaignType())
                .triggerFlow(triggerFlow)
                .build();

        if (triggerRe.isSendAll()) {
            List<Long> partnerIds = organizationCollaborationRepository.
                    findActivePartnerIds(triggerRe.getOrganizationId());
            triggerRe.setPartnerIds(partnerIds);
        }

        List<Condition> conditions = triggerRe.getTriggerFlow().getConditions();
        conditions.forEach(condition -> condition.setTriggerFlow(triggerRe.getTriggerFlow()));

        var trigger = generalTriggerRepository.save(triggerRe);
        var resp = GeneralTriggerBaseResponse.builder()
                .id(trigger.getId())
                .organizationId(trigger.getOrganizationId())
                .campaignType(trigger.getCampaignType())
                .campaignName(trigger.getCampaignName())
                .status(trigger.getStatus())
                .creationTime(trigger.getCreationTimestamp())
                .build();
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    public Map<String, String> updateTrigger(TriggerUpdateRequest request) {
        GeneralTrigger trigger = generalTriggerRepository.findById(request.triggerId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH14, request.triggerId()));
        trigger.setStatus(request.status());
        trigger.getTriggerFlow().setEdges(request.edges());
        trigger.getTriggerFlow().setNodes(request.nodes());
        trigger.setCampaignName(request.campaignName());
        trigger.getTriggerFlow().setConditions(request.conditions());
        generalTriggerRepository.save(trigger);
        if (trigger.getCampaignType().equals(CampaignType.PRODUCT_UPDATE) && request.status().equals(TriggerStatus.ACTIVE)) {
            var conditions = trigger.getTriggerFlow().getConditions();
            conditions.forEach(condition -> triggerEmail(trigger, condition));
        }

        return Map.of("message", request.status() == TriggerStatus.ACTIVE ? "Trigger Scheduled" : "Trigger Paused");
    }

    public List<GeneralTriggerBaseResponse> getTriggersByOrgId(long organizationId) {
        var triggers = generalTriggerRepository.findAllByOrganizationId(organizationId);

        return triggers.stream().map(trigger -> GeneralTriggerBaseResponse.builder()
                .id(trigger.getId())
                .organizationId(trigger.getOrganizationId())
                .campaignType(trigger.getCampaignType())
                .campaignName(trigger.getCampaignName())
                .status(trigger.getStatus())
                .creationTime(trigger.getCreationTimestamp())
                .build()
        ).toList();
    }

    public GeneralTriggerResponse getTriggerById(long triggerId) {
        var trigger = generalTriggerRepository.findById(triggerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH14, triggerId));

        var triggerResponse = GeneralTriggerResponse.builder()
                .organizationId(trigger.getOrganizationId())
                .triggerFlow(trigger.getTriggerFlow())
                .sendAll(trigger.isSendAll())
                .campaignName(trigger.getCampaignName())
                .campaignType(trigger.getCampaignType())
                .status(trigger.getStatus())
                .build();

        var partners = organizationRepository.findAllByIdIn(trigger.getPartnerIds());
        List<PartnerDetail> partnerDetails = partners.stream().map(partner -> PartnerDetail.builder()
                .orgEmail(partner.getPrimaryEmail())
                .orgId(partner.getId())
                .orgName(partner.getName())
                .build()).toList();

        triggerResponse.setAssignedPartners(partnerDetails);
        if (trigger.isSendAll()) {
            triggerResponse.setActivePartners(partnerDetails);
        } else {
            List<Long> activePartnerIds = organizationCollaborationRepository.
                    findActivePartnerIds(trigger.getOrganizationId());
            var activePartners = organizationRepository.findAllByIdIn(activePartnerIds);
            triggerResponse.setActivePartners(activePartners.stream().map(partner -> PartnerDetail.builder()
                    .orgEmail(partner.getPrimaryEmail())
                    .orgId(partner.getId())
                    .orgName(partner.getName())
                    .build()).toList());
        }

        return triggerResponse;
    }

    public Map<String, Long> createTriggerTemplate(TriggerTemplateRequest templateRequest) {
        TriggerTemplate triggerTemplate = TriggerTemplate.builder()
                .userId(templateRequest.getUserId())
                .body(templateRequest.getBody())
                .subject(templateRequest.getSubject())
                .build();

        Long id = triggerTemplateRepository.save(triggerTemplate).getId();
        return Map.of("id", id);
    }

    public List<TriggerTemplate> getTriggerTemplates(String userId) {
        return triggerTemplateRepository.findAllByUserId(userId);
    }

    public Optional<TriggerTemplate> getTriggerTemplateById(Long templateId) {
        return triggerTemplateRepository.findById(templateId);
    }

    @Scheduled(cron = "0 0 11 * * ?", zone = "Asia/Kolkata")
    public void triggerEmailForMatchedDate() {
        var triggers = generalTriggerRepository.findAllByStatus(TriggerStatus.ACTIVE);
        triggers.forEach(trigger -> {
            var conditions = trigger.getTriggerFlow().getConditions();
            conditions.stream()
                    .filter(condition -> {
                        LocalDate creationDate = condition.getCreationTimestamp().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return LocalDate.now().isEqual(creationDate.plusDays(condition.getDelay()));
                    })
                    .forEach(condition -> triggerEmail(trigger, condition));
        });
    }

    private void triggerEmail(GeneralTrigger trigger, Condition condition) {
        var recipients = getEmailRecipients(trigger, condition);
        if (recipients.isEmpty()) {
            return;
        }

        var template = triggerTemplateRepository.findById(condition.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH15, condition.getTemplateId()));

        try {
            amazonSes.prepareAndSend(template.getSubject(), template.getBody(),
                    null,
                    defaultSender,
                    String.join(",", recipients),
                    null,
                    String.valueOf(condition.getTemplateId()),
                    trigger.getOrganizationId());
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    private List<String> getEmailRecipients(GeneralTrigger trigger, Condition condition) {
        if (condition.getActiveFor() > 0) {
            List<String> emails = new ArrayList<>();
            trigger.getPartnerIds()
                    .forEach(partnerId -> {
                        String partnerEmail = organizationRepository.findEmailById(partnerId);
                        var orgCollab = organizationCollaborationRepository.
                                findBySenderOrganizationIdOrReceiverOrganizationId(trigger.getOrganizationId(), partnerId);

                        LocalDate creationDate = orgCollab.getCreationTimestamp().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        if (LocalDate.now().isAfter(creationDate.plusMonths(condition.getActiveFor()))) {
                            emails.add(partnerEmail);
                        }
                    });
            return emails;
        } else {
            return organizationRepository.findEmailsByIdIn(trigger.getPartnerIds());
        }
    }

    public CampaignResponse saveCampaign(Long organizationId, Campaign campaign) {
        var existingCampaignOptional = campaignIntegrationRepository.findByOrganizationIdAndCampaignType(organizationId, campaign.campaignType);
        if (existingCampaignOptional.isPresent()) {
            var existingCampaign = existingCampaignOptional.get();
            existingCampaign.setCampaignValue(campaign.toString());
            campaignIntegrationRepository.save(existingCampaign);
        } else {
            var newCampaign = CampaignIntegrationEntity.builder()
                    .organizationId(organizationId)
                    .campaignValue(campaign.toString())
                    .campaignType(campaign.campaignType)
                    .build();
            campaignIntegrationRepository.save(newCampaign);
        }
        List<Long> activePartnerIds = organizationCollaborationRepository.
                findActivePartnerIds(organizationId);
        var activePartners = organizationRepository.findAllByIdIn(activePartnerIds);
        var gson = new Gson();
        var campaignResponse = gson.fromJson(campaign.toString(), CampaignResponse.class);
        campaignResponse.setActivePartners(activePartners.stream().map(partner -> PartnerDetail.builder()
                .orgEmail(partner.getPrimaryEmail())
                .orgId(partner.getId())
                .orgName(partner.getName())
                .build()).toList());
        campaignResponse.setOrganizationId(organizationId);
        return campaignResponse;
    }

    public CampaignResponse getCampaign(Long organizationId, CampaignType campaignType) {
        var existingCampaignOptional = campaignIntegrationRepository.findByCampaignType(campaignType);
        if (existingCampaignOptional.isPresent()) {
            List<Long> activePartnerIds = organizationCollaborationRepository.
                    findActivePartnerIds(organizationId);
            var activePartners = organizationRepository.findAllByIdIn(activePartnerIds);
            var gson = new Gson();
            var campaignResponse = gson.fromJson(existingCampaignOptional.get().getCampaignValue(), CampaignResponse.class);
            campaignResponse.setActivePartners(activePartners.stream().map(partner -> PartnerDetail.builder()
                    .orgEmail(partner.getPrimaryEmail())
                    .orgId(partner.getId())
                    .orgName(partner.getName())
                    .build()).toList());
            campaignResponse.setOrganizationId(organizationId);
            return campaignResponse;
        }
        return null;
    }
}
