package com.sharkdom.service.partnermapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sharkdom.dto.MyPartnerMappingResponse;
import com.sharkdom.entity.ai.PersonaStatusEntity;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.entity.partnermapping.MyPartnerMappingReportStatus;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.organizatiocollaboration.CollaborationStatus;
import com.sharkdom.offlinePartner.entity.OfflinePartnerInvite;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.partnermapping.MyPartnerMappingReportStatusRepository;
import com.sharkdom.service.ai.PersonaService;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationService;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;


@Slf4j
@Service
public class PartnerMappingService {

    @Autowired
    private OrganizationCollaborationRepository organizationCollaborationRepository;

    @Autowired
    private OfflinePartnerInviteRepository offlinePartnerInviteRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PersonaStatusRepository personaStatusRepository;

    @Autowired
    private OrganizationCollaborationService organizationCollaborationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private MyPartnerMappingReportStatusRepository myPartnerMappingReportStatusRepository;

    @Transactional
    public JSONObject  getPartnerMappingDetails() throws JSONException {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Getting partner mapping details for partner id: {}", orgIdFromToken);

        JSONObject partnerMappingDetails = new JSONObject();

        try {
            long myPartnersCount = safeCount(() -> countMyPartners(orgIdFromToken));
            long externalPartnersCount = safeCount(() -> countExternalPartners(orgIdFromToken));
            int overlapsCount = (int) safeCount(() -> (long) getOverlapsCount(orgIdFromToken));
            double overlapsRate = getOverlapsRate(orgIdFromToken);
            long reportGenerated = safeCount(this::getReportGenerateCount);

            partnerMappingDetails.put("report_generated", reportGenerated);
            partnerMappingDetails.put("total_overlaps_rate", overlapsRate);
            partnerMappingDetails.put("total_overlaps", overlapsCount);
            partnerMappingDetails.put("active_partners", myPartnersCount + externalPartnersCount);
            partnerMappingDetails.put("organization_id", orgIdFromToken);

            List<MyPartnerMappingResponse> partners = Optional.ofNullable(getOverlapsPartner(orgIdFromToken))
                    .orElse(Collections.emptyList());

            JSONArray partnersArray = new JSONArray();
            for (MyPartnerMappingResponse p : partners) {
                if (p == null) continue;
                JSONObject obj = new JSONObject();

                obj.put("partnerOrganizationId", safe(p.getPartnerOrganizationId()));
                obj.put("organizationName", safe(p.getOrganizationName()));
                obj.put("logoUrl", safe(p.getLogoUrl()));
                obj.put("overlapRate", safeDouble(() -> (Number) p.getOverlapRate()));

                boolean dataSourceConnected = personaStatusRepository
                        .findFirstByOrganizationIdAndPersonaStatus(
                                p.getPartnerOrganizationId(), PersonaStatus.COMPLETED)
                        .isPresent();

                obj.put("dataSourceConnected", dataSourceConnected);
                obj.put("aProspectOverlapCount", safeCount(() -> (Number) p.getAProspectOverlapCount()));
                obj.put("aCustomerOverlapCount", safeCount(() -> (Number) p.getACustomerOverlapCount()));
                obj.put("aOpportunityOverlapCount", safeCount(() -> (Number) p.getAOpportunityOverlapCount()));


                partnersArray.put(obj);
            }

            partnerMappingDetails.put("my_partners", partnersArray);

        } catch (Exception ex) {
            log.error("Error building partner mapping details for orgId: {}", orgIdFromToken, ex);
            partnerMappingDetails.put("error", "Some data could not be fetched");
        }

        return partnerMappingDetails;
    }

    public JSONObject getPartnerMappingReportDetails(String type) throws JSONException {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Getting partner mapping details for partner id: {}", orgIdFromToken);
        JSONObject partnerMappingDetails = new JSONObject();
        partnerMappingDetails.put("organization_id",orgIdFromToken);
        // Convert MyPartnerMappingResponse list to JSONArray
        List<MyPartnerMappingResponse> partners = getOverlapsPartner(orgIdFromToken);
        JSONArray partnersArray = new JSONArray();
        for (MyPartnerMappingResponse p : partners) {
            JSONObject obj = new JSONObject();
            obj.put("partnerOrganizationId", p.getPartnerOrganizationId());
            obj.put("organizationName", p.getOrganizationName());
            obj.put("logoUrl", p.getLogoUrl());
            Optional<PersonaStatusEntity> optPersona = personaStatusRepository.findFirstByOrganizationIdAndPersonaStatus(p.getPartnerOrganizationId(), PersonaStatus.COMPLETED);
            if(optPersona.isPresent())
            {
                obj.put("dataSourceConnected", true);
            }
            else
            {
                obj.put("dataSourceConnected", false);
            }
            Map<String, Object> partnerDataWithPermissions = personaService.getPartnerDataWithPermissions(p.getPartnerOrganizationId(), type);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.convertValue(partnerDataWithPermissions, ObjectNode.class);
            obj.put("report", jsonObject);
            partnersArray.put(obj);
        }
        partnerMappingDetails.put("my_partners_data", partnersArray);
        return partnerMappingDetails;
    }

    public JSONObject getPartnerMappingReportDetailsForEPS(String type) throws JSONException {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Getting partner mapping details for partner id: {}", orgIdFromToken);
        JSONObject partnerMappingDetails = new JSONObject();
        partnerMappingDetails.put("organization_id",orgIdFromToken);
        // Convert MyPartnerMappingResponse list to JSONArray
        List<MyPartnerMappingResponse> partners = getOverlapsPartner(orgIdFromToken);
        JSONArray partnersArray = new JSONArray();
        for (MyPartnerMappingResponse p : partners) {
            JSONObject obj = new JSONObject();
            obj.put("partnerOrganizationId", p.getPartnerOrganizationId());
            obj.put("organizationName", p.getOrganizationName());
            obj.put("logoUrl", p.getLogoUrl());
            Optional<PersonaStatusEntity> optPersona = personaStatusRepository.findFirstByOrganizationIdAndPersonaStatus(p.getPartnerOrganizationId(), PersonaStatus.COMPLETED);
            if(optPersona.isPresent())
            {
                obj.put("dataSourceConnected", true);
            }
            else
            {
                obj.put("dataSourceConnected", false);
            }
            Map<String, Object> partnerDataWithPermissions = personaService.getPartnerDataWithPermissions(p.getPartnerOrganizationId(), type);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.convertValue(partnerDataWithPermissions, ObjectNode.class);
            obj.put("report", jsonObject);
            partnersArray.put(obj);
        }
        partnerMappingDetails.put("my_partners_data", partnersArray);
        return partnerMappingDetails;
    }

    private long safeCount(Supplier<Number> supplier) {
        try {
            return Optional.ofNullable(supplier.get()).map(Number::longValue).orElse(0L);
        } catch (Exception e) {
            log.warn("Count fetch failed: {}", e.getMessage());
            return 0L;
        }
    }

    private double safeDouble(Supplier<Number> supplier) {
        try {
            Number value = supplier.get();
            double d = (value != null) ? value.doubleValue() : 0.0;

            // Handle Infinity, NaN, or overflowed values
            if (Double.isNaN(d) || Double.isInfinite(d) || d > 1_000_000_000) {
                log.warn("Invalid double value detected: {}", d);
                return 0.0;
            }
            return d;
        } catch (Exception e) {
            log.warn("Double fetch failed: {}", e.getMessage());
            return 0.0;
        }
    }


    private Object safe(Object value) {
        return value != null ? value : JSONObject.NULL;
    }

    public JSONObject getPartnerMappingSummary(String typeCombination, Long PartnerId) throws JSONException {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Getting partner mapping summary for organization id: {}", orgIdFromToken);
        JSONObject partnerMappingSummary = new JSONObject();

        return partnerMappingSummary;
    }

    public double getOverlapsRate(Long organizationId) {
        log.info("Calculating overlaps rate for organization id: {}", organizationId);

        List<OrganizationCollaboration> activePartners = organizationCollaborationService.getAllCollaboration();
        if (activePartners == null || activePartners.isEmpty()) {
            log.warn("No active partners found for organization id: {}", organizationId);
            return 0.0;
        }

        ObjectMapper mapper = new ObjectMapper();
        double finalTotal = 0.0;
        int processedPartners = 0;

        for (OrganizationCollaboration collaboration : activePartners) {
            if (collaboration == null) {
                log.warn("Encountered null collaboration object, skipping...");
                continue;
            }

            Long activePartnerId = collaboration.getReceiverOrganizationId();
            if (activePartnerId == null) {
                log.warn("Receiver organization id is null, skipping...");
                continue;
            }

            Map<String, Object> partnerDataWithPermissions;
            try {
                partnerDataWithPermissions = personaService.getPartnerDataWithPermissions(activePartnerId, null);
            } catch (Exception ex) {
                log.error("Error while fetching partner data for partnerId: {}", activePartnerId, ex);
                continue;
            }

            if (partnerDataWithPermissions == null || partnerDataWithPermissions.isEmpty()) {
                log.warn("No partner data found for partnerId: {}", activePartnerId);
                continue;
            }

            JsonNode root = mapper.valueToTree(partnerDataWithPermissions);
            if (root == null) {
                log.warn("Failed to convert partner data to JSON tree for partnerId: {}", activePartnerId);
                continue;
            }

            int partnerOverlap = 0;
            for (JsonNode countNode : root.findValues("overlap_count")) {
                if (countNode != null && countNode.isInt()) {
                    partnerOverlap += countNode.asInt();
                }
            }

            int rawRecords = 0;
            for (JsonNode node : root.path("matrix").path("A_PROSPECTS").findValues("raw_records_sum")) {
                if (node != null && node.isInt()) {
                    rawRecords += node.asInt();
                }
            }

            if (rawRecords <= 0) {
                log.warn("Raw records is zero or invalid for partnerId: {}, skipping calculation", activePartnerId);
                continue;
            }

            double normalizedPercentage = ((double) partnerOverlap / rawRecords) * 100.0;
            log.debug("Partner {} normalized overlap = {}", activePartnerId, normalizedPercentage);

            // Prevent adding impossible values
            if (Double.isFinite(normalizedPercentage) && normalizedPercentage >= 0 && normalizedPercentage <= 100) {
                finalTotal += normalizedPercentage;
                processedPartners++;
            } else {
                log.warn("Invalid normalized percentage {} for partnerId: {}, skipping", normalizedPercentage, activePartnerId);
            }
        }

        if (processedPartners == 0) {
            log.warn("No valid partners processed for organization id: {}", organizationId);
            return 0.0;
        }

        double result = finalTotal / processedPartners;
        double roundedResult = Math.round(result * 10.0) / 10.0;

        log.info("Final overlap rate for organization {} = {}", organizationId, roundedResult);
        return roundedResult;
    }


    public long countMyPartners(Long organizationId) {
        log.info("Counting active collaborations for sender organization id: {}", organizationId);
        // Fetch the list of active collaborations
        Long l = organizationCollaborationService.getAllCollaborationsCount(CollaborationStatus.ACTIVE);
        log.info("Found {} active collaborations for sender organization id: {}", l, organizationId);
        // Return the count
        return l;
    }

    public long countExternalPartners(Long organizationId) {
        log.info("Counting offline partner invites for organization id: {}", organizationId);
        // Fetch the list of offline partner invites
        List<OfflinePartnerInvite> invites =
                offlinePartnerInviteRepository.findByOrganizationId(organizationId);
        log.info("Found {} offline partner invites for organization id: {}", invites.size(), organizationId);
        // Return the count
        return invites.size();
    }

    public int countCRMConnected(List<Long> organizationIds) {
        log.info("Fetching IntegrationDetails for organization ids: {}", organizationIds);
        List<IntegrationDetails> details = integrationRepository.findAllByOrganizationIdIn(organizationIds);
        log.info("Found {} IntegrationDetails for organization ids: {}", details.size(), organizationIds);
        return details.size();
    }

    public int getOverlapsCount(Long organizationId) {
        log.info("Fetching overlaps for organization id: {}", organizationId);

        // Get all active collaborations
        List<OrganizationCollaboration> collaborations = organizationCollaborationService.getAllCollaboration();
        log.info("Found {} active collaborations for sender organization id: {}", collaborations.size(), organizationId);

        // Get partner organization IDs
        List<Long> partnerIds = new ArrayList<>();
        log.info("Extracting partner organization IDs...");
        for(OrganizationCollaboration organizationCollaboration:collaborations)
        {
            if(organizationCollaboration.getSenderOrganizationId()==organizationId)
            {
                partnerIds.add(organizationCollaboration.getReceiverOrganizationId());
            }
            else if(organizationCollaboration.getReceiverOrganizationId()==organizationId)
            {
                partnerIds.add(organizationCollaboration.getSenderOrganizationId());
            }
        }
        log.info("Extracted {} partner organization IDs: {}", partnerIds.size(), partnerIds);
        // Count PersonaEntity records for each partner organization
        List<PersonaStatusEntity> entities = personaStatusRepository.findByOrganizationIdInAndPersonaStatus(partnerIds, PersonaStatus.COMPLETED);
        log.info("Total Persona records for partner organizations: {}", entities.size());
        return entities.size();
    }

    public List<MyPartnerMappingResponse> getOverlapsPartner(Long organizationId) {
        log.info("Fetching overlaps for organization id: {}", organizationId);

        ObjectMapper mapper = new ObjectMapper();
        List<MyPartnerMappingResponse> result = new ArrayList<>();

        // Get all active collaborations
        List<OrganizationCollaboration> collaborations = organizationCollaborationService.getAllCollaboration();
        log.info("Found {} active collaborations for organization id: {}", collaborations.size(), organizationId);

        // Extract partner organization IDs
        List<Long> partnerIds = new ArrayList<>();
        for (OrganizationCollaboration collab : collaborations) {
            if (Objects.equals(collab.getSenderOrganizationId(), organizationId)) {
                partnerIds.add(collab.getReceiverOrganizationId());
            } else if (Objects.equals(collab.getReceiverOrganizationId(), organizationId)) {
                partnerIds.add(collab.getSenderOrganizationId());
            }
        }
        log.info("Extracted {} partner organization IDs: {}", partnerIds.size(), partnerIds);

        // Process each partner
        for (Long partnerId : partnerIds) {
            log.info("Processing partner ID: {}", partnerId);

            Map<String, Object> partnerDataWithPermissions;
            try {
                partnerDataWithPermissions = personaService.getPartnerDataWithPermissions(partnerId, null);
            } catch (Exception ex) {
                log.error("Error while fetching partner data for partnerId: {}", partnerId, ex);
                continue;
            }

            if (partnerDataWithPermissions == null || partnerDataWithPermissions.isEmpty()) {
                log.warn("No partner data found for partnerId: {}", partnerId);
                continue;
            }

            JsonNode root = mapper.valueToTree(partnerDataWithPermissions);
            if (root == null) {
                log.warn("Failed to convert partner data for partnerId: {}", partnerId);
                continue;
            }

            int partnerOverlap = root.findValues("overlap_count").stream()
                    .filter(JsonNode::isInt)
                    .mapToInt(JsonNode::asInt)
                    .sum();

            int rawRecords = root.path("matrix").path("A_PROSPECTS")
                    .findValues("raw_records_sum").stream()
                    .filter(JsonNode::isInt)
                    .mapToInt(JsonNode::asInt)
                    .sum();

            int sumProspects = root.path("matrix").path("A_PROSPECTS")
                    .findValues("overlap_count").stream()
                    .filter(JsonNode::isInt)
                    .mapToInt(JsonNode::asInt)
                    .sum();

            int sumOpportunities = root.path("matrix").path("A_OPPORTUNITIES")
                    .findValues("overlap_count").stream()
                    .filter(JsonNode::isInt)
                    .mapToInt(JsonNode::asInt)
                    .sum();

            int sumCustomers = root.path("matrix").path("A_CUSTOMERS")
                    .findValues("overlap_count").stream()
                    .filter(JsonNode::isInt)
                    .mapToInt(JsonNode::asInt)
                    .sum();

            // Avoid division by zero
            double normalizedPercentage = (rawRecords > 0)
                    ? ((double) partnerOverlap / rawRecords * 100)
                    : 0.0;

            Optional<Organization> optOrganization = organizationRepository.findById(partnerId);
            if (optOrganization.isEmpty()) {
                log.warn("No organization found for partnerId: {}", partnerId);
                continue;
            }

            Organization organization = optOrganization.get();
            MyPartnerMappingResponse response = new MyPartnerMappingResponse();
            response.setOrganizationName(organization.getName());
            response.setPartnerOrganizationId(organization.getId());
            response.setLogoUrl(organization.getLogoUrl());
            response.setOverlapRate(normalizedPercentage);
            response.setACustomerOverlapCount(sumCustomers);
            response.setAProspectOverlapCount(sumProspects);
            response.setAOpportunityOverlapCount(sumOpportunities);

            result.add(response);
            log.info("Added partner {} ({}) with overlap rate: {}%",
                    organization.getName(), organization.getId(), normalizedPercentage);
        }

        return result;
    }


    public JSONObject savePartnerMappingReport(MyPartnerMappingReportStatus reportStatus) {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Saving partner mapping report status for organization id: {}", reportStatus.getOrganizationId());
        JSONObject response = new JSONObject();
        try {
            reportStatus.setOrganizationId(orgIdFromToken);
            MyPartnerMappingReportStatus savedStatus = myPartnerMappingReportStatusRepository.save(reportStatus);
            response.put("status", "success");
            response.put("message", "Report status saved successfully");
        } catch (Exception ex) {
            log.error("Error while saving partner mapping report status for organization id: {}",
                    reportStatus.getOrganizationId(), ex);
            try {
                response.put("status", "error");
                response.put("message", "Failed to save report status");
            } catch (JSONException jsonEx) {
                log.error("Error while constructing error response JSON", jsonEx);
            }
        }
        return response;
    }

    public JSONObject getPartnerMappingReportStatus() throws JSONException {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Fetching partner mapping report status for organization id: {}", orgIdFromToken);
        JSONObject response = new JSONObject();
        try {
            List<MyPartnerMappingReportStatus> reportStatusList =
                    myPartnerMappingReportStatusRepository.findByOrganizationId(orgIdFromToken);

            JSONArray jsonArray = new JSONArray();

            for (MyPartnerMappingReportStatus status : reportStatusList) {
                JSONObject statusJson = new JSONObject();

                // MyPartnerMappingReportStatus fields
                statusJson.put("id", status.getId());
                statusJson.put("organizationId", status.getOrganizationId());
                statusJson.put("partnerId", status.getPartnerId());
                statusJson.put("yourMatrix", status.getYourMatrix());
                statusJson.put("partnerMatrix", status.getPartnerMatrix());
                statusJson.put("overlapCount", status.getOverlapCount());
                statusJson.put("reportCount", status.getReportCount());
                statusJson.put("createdAt",status.getCreationTimestamp());

                // Associated Partner Organization
                Optional<Organization> optOrganization = organizationRepository.findById(status.getPartnerId());
                if (optOrganization.isPresent()) {
                    Organization partnerOrg = optOrganization.get();
                    JSONObject partnerJson = new JSONObject();
                    partnerJson.put("partnerId", partnerOrg.getId());
                    partnerJson.put("partnerName", partnerOrg.getName());
                    partnerJson.put("partnerLogoUrl", partnerOrg.getLogoUrl());
                    statusJson.put("partnerOrganization", partnerJson);
                } else {
                    statusJson.put("partnerOrganization", JSONObject.NULL);
                }
                jsonArray.put(statusJson);
            }
            response.put("data", jsonArray);
            response.put("count", reportStatusList.size());
            response.put("success", true);
        } catch (Exception ex) {
            log.error("Error while fetching partner mapping report status for organization id: {}", orgIdFromToken, ex);
            response.put("success", false);
            response.put("message", "Error while fetching partner mapping report status");
        }
        return response;
    }

    public Long getReportGenerateCount()
    {
        log.info("Getting report generate count");
        Long orgIdFromToken = Util.getOrgIdFromToken();
        return myPartnerMappingReportStatusRepository.countByOrganizationId(orgIdFromToken);
    }

    public Long getReportGenerateCountForUser(String userId)
    {
        log.info("Getting report generate count");
        return myPartnerMappingReportStatusRepository.countByUserId(userId);
    }

    public JSONObject savePartnerMappingReportForUser(MyPartnerMappingReportStatus reportStatus) {
        String userId=reportStatus.getUserId();
        log.info("Saving partner mapping report status for organization id: {}", reportStatus.getOrganizationId());
        JSONObject response = new JSONObject();
        try {
            reportStatus.setOrganizationId(0l);
            reportStatus.setUserId(userId);
            MyPartnerMappingReportStatus savedStatus = myPartnerMappingReportStatusRepository.save(reportStatus);
            response.put("status", "success");
            response.put("message", "Report status saved successfully");
        } catch (Exception ex) {
            log.error("Error while saving partner mapping report status for organization id: {}",
                    reportStatus.getOrganizationId(), ex);
            try {
                response.put("status", "error");
                response.put("message", "Failed to save report status");
            } catch (JSONException jsonEx) {
                log.error("Error while constructing error response JSON", jsonEx);
            }
        }
        return response;
    }

    public JSONObject getPartnerMappingReportStatusForUser(String userId) throws JSONException {
        log.info("Fetching partner mapping report status for user id: {}", userId);
        JSONObject response = new JSONObject();
        try {
            List<MyPartnerMappingReportStatus> reportStatusList =
                    myPartnerMappingReportStatusRepository.findByUserId(userId);

            JSONArray jsonArray = new JSONArray();

            for (MyPartnerMappingReportStatus status : reportStatusList) {
                JSONObject statusJson = new JSONObject();

                // MyPartnerMappingReportStatus fields
                statusJson.put("id", status.getId());
                statusJson.put("organizationId", status.getOrganizationId());
                statusJson.put("partnerId", status.getPartnerId());
                statusJson.put("yourMatrix", status.getYourMatrix());
                statusJson.put("partnerMatrix", status.getPartnerMatrix());
                statusJson.put("overlapCount", status.getOverlapCount());
                statusJson.put("reportCount", status.getReportCount());
                statusJson.put("createdAt",status.getCreationTimestamp());

                // Associated Partner Organization
                Optional<Organization> optOrganization = organizationRepository.findById(status.getPartnerId());
                if (optOrganization.isPresent()) {
                    Organization partnerOrg = optOrganization.get();
                    JSONObject partnerJson = new JSONObject();
                    partnerJson.put("partnerId", partnerOrg.getId());
                    partnerJson.put("partnerName", partnerOrg.getName());
                    partnerJson.put("partnerLogoUrl", partnerOrg.getLogoUrl());
                    statusJson.put("partnerOrganization", partnerJson);
                } else {
                    statusJson.put("partnerOrganization", JSONObject.NULL);
                }
                jsonArray.put(statusJson);
            }
            response.put("data", jsonArray);
            response.put("count", reportStatusList.size());
            response.put("success", true);
        } catch (Exception ex) {
            log.error("Error while fetching partner mapping report status for organization id: {}", userId, ex);
            response.put("success", false);
            response.put("message", "Error while fetching partner mapping report status");
        }
        return response;
    }

}
