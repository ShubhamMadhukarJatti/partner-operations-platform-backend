package com.sharkdom.service.organization;

import com.sharkdom.config.AppProperties;
import com.sharkdom.constants.organization.OrgUserMappingRequestStatus;
import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.organization.OrganizationUserMappingRequest;
import com.sharkdom.entity.user.User;
import com.sharkdom.model.email.TemplateEmailReqModel;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRequestRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.user.UserService;
import com.sharkdom.util.GeneralUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrganizationUserMappingRequestService {

    @Autowired
    private OrganizationUserMappingRequestRepository organizationUserMappingRequestRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    UserService userService;
    @Autowired
    AppProperties appProperties;
    @Value("${app.environment.proxy_url}")
    private String proxyBaseUrl;
    @Autowired
    private OrganizationUserMappingRepository organizationUserMappingRepository;
    @Autowired
    GeneralUtils generalUtils;


    @Transactional
    public OrganizationUserMappingRequest save(OrganizationUserMappingRequest organizationRequestMapping) {
        return organizationUserMappingRequestRepository.save(organizationRequestMapping);
    }


    public void saveRequestMappingAndSendApprovalMail(OrganizationUserMapping organizationUserMapping) {
        OrganizationUserMappingRequest organizationRequestMapping = new OrganizationUserMappingRequest(organizationUserMapping);
        save(organizationRequestMapping);
        sendApprovalMail(organizationUserMapping, organizationRequestMapping.getRequestId());
    }

    @Async
    private void sendApprovalMail(OrganizationUserMapping organizationUserMapping, String requestId) {
        try { //find all active admin
            List<User> adminUsers = organizationUserMappingRepository
                    .findAllByOrganizationIdAndRoleAndStatus(organizationUserMapping.getOrganizationId(),
                            OrgUserRole.ADMIN, OrgUserMappingStatus.ACTIVE).stream().map(res ->
                            res.getUser()).collect(Collectors.toList());

            log.info("inside saveRequestMappingAndSendApprovalMail");
            List<String> adminUserIds = adminUsers.stream().map(adminUser -> adminUser.getUserId()).collect(Collectors.toList());

            Map<String, Map<String, Object>> additionalDataMap = getAdditionalDataMapVerificationEmail(organizationUserMapping, adminUsers, requestId);
            TemplateEmailReqModel templateEmailReqModel =
                    new TemplateEmailReqModel(appProperties.getEmailTemplateCodeForEvent("ORG_USER_MAP_APPROVAL_REQUEST_EMAIL"), adminUserIds, null,null, null, null, null, null, null, null, null, null, null, null, null, null);
            emailService.sendByTemplateAndUserIds(templateEmailReqModel, additionalDataMap);
        } catch (Exception e) {
            log.error("Failed to sent email for new org-user-mapping for user with userId " + organizationUserMapping.getUserId());
            //TODO add log in logger table??
        }

    }

    public Map<String, Map<String, Object>> getAdditionalDataMapVerificationEmail(OrganizationUserMapping organizationUserMapping, List<User> adminUsers, String requestId) {

        Map<String, Object> organizationDataMap = getOrganizationDataMap(organizationUserMapping.getOrganizationId());

        Map<String, Object> newUserDataMap = getUserDataMap(organizationUserMapping.getUserId());

        Map<String, Object> orgMappingDataMap = generalUtils.convetObjectToMap(organizationUserMapping, "orgMapping.");

        //Combine all maps together
        final Map<String, Object> dataMap = orgMappingDataMap;
        dataMap.putAll(organizationDataMap);
        dataMap.putAll(newUserDataMap);

        //Create Map of Map where keys of parent map is admin user Ids and child map has same value for all of them as the organization and newUser data will be sent same to all admins
        return adminUsers.stream().map(adminUser -> {
                    //Add approve and reject endpoints in the dataMap
                    String endpoint = proxyBaseUrl + "service/approveUser/" + requestId + "/" + String.valueOf(adminUser.getId());
                    String approveRequestEndpoint = endpoint + "/approve";
                    String rejectRequestEndpoint = endpoint + "/reject";
                    dataMap.put("approveRequestEndpoint", approveRequestEndpoint);
                    dataMap.put("rejectRequestEndpoint", rejectRequestEndpoint);
                    return new HashMap.SimpleEntry<>(adminUser.getUserId(), dataMap);
                }
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Object> getUserDataMap(String userId) {
        User newUser = userService.findByUserId(userId).getBody();
        Map<String, Object> newUserDataMap = generalUtils.convetObjectToMap(newUser, "newUser.");
        return newUserDataMap;
    }

    private Map<String, Object> getOrganizationDataMap(long organizationId) {
        Optional<Organization> organization = organizationService.findById(organizationId);
        Map<String, Object> organizationDataMap = generalUtils.convetObjectToMap(organization, "organization.");
        return organizationDataMap;
    }


    @Transactional
    public void action(String requestId, long actionedByUserFk, String action) {

        Optional<OrganizationUserMappingRequest> OptionalRequest = organizationUserMappingRequestRepository.findOneByRequestId(requestId);

        OrgUserMappingRequestStatus newstatus = OrgUserMappingRequestStatus.UNAPPROVED;

        switch (action) {
            case "approve":
                newstatus = OrgUserMappingRequestStatus.ACTIVE;
                break;
            case "reject":
                newstatus = OrgUserMappingRequestStatus.REJECTED;
                break;
        }

        if (OptionalRequest.isPresent() && OptionalRequest.get().getStatus() != OrgUserMappingRequestStatus.ACTIVE) {
            OrganizationUserMappingRequest request = OptionalRequest.get();
            request.setStatus(newstatus);
            request.setActionedByUserFk(actionedByUserFk);
            organizationUserMappingRequestRepository.save(request);

            Optional<OrganizationUserMapping> optionalOrganizationUserMapping =
                    organizationUserMappingRepository.findByOrganizationIdAndUserId(request.getOrganizationId(), request.getUserId());

            if (optionalOrganizationUserMapping.isPresent()) {
                OrganizationUserMapping organizationUserMapping = optionalOrganizationUserMapping.get();

                //if approval request then change status of mapping table else do nothing
                if (OrgUserMappingRequestStatus.ACTIVE == newstatus) {
                    organizationUserMapping.setStatus(OrgUserMappingStatus.ACTIVE);
                    organizationUserMapping.setApprovedByUserFk(actionedByUserFk);
                    organizationUserMappingRepository.save(organizationUserMapping);
                }
                //Send acceptance or rejection email
                sendActionEmail(organizationUserMapping, newstatus);
            }
        }
    }

    @Async
    private void sendActionEmail(OrganizationUserMapping mapping, OrgUserMappingRequestStatus action) {

        Map<String, Object> organizationDataMap = getOrganizationDataMap(mapping.getOrganizationId());
        Map<String, Object> orgMappingDataMap = generalUtils.convetObjectToMap(mapping, "orgMapping.");

        final Map<String, Object> additionalDataMap = organizationDataMap;
        additionalDataMap.putAll(orgMappingDataMap);

        String templateName = OrgUserMappingRequestStatus.ACTIVE == action ? appProperties.getEmailTemplateCodeForEvent("ORG_USER_MAP_ACCEPTED") :
                appProperties.getEmailTemplateCodeForEvent("ORG_USER_MAP_REJECTED");

        TemplateEmailReqModel templateEmailReqModel =
                new TemplateEmailReqModel(templateName, List.of(mapping.getUserId()), null,null, null, null, null, null, null, null, null, null, null, null, null, null);
        emailService.sendByTemplateAndUserIds(templateEmailReqModel, Map.of(mapping.getUserId(), additionalDataMap));
    }
}
