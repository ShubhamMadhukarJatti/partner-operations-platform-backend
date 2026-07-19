package com.sharkdom.zoho.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.MouStatus;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.repository.configuration.ConfigurationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.zoho.entity.ZohoSignedDocumentEntity;
import com.sharkdom.zoho.repository.ZohoSignedDocumentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ZohoDocumentService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final OrganizationRepository organizationRepository;
    private final ZohoSignedDocumentRepository zohoSignedDocumentRepository;
    private final OfflinePartnerInviteRepository offlinePartnerInviteRepository;
    private final ConfigurationRepository configurationRepository;

    public ZohoDocumentService(OrganizationCollaborationRepository organizationCollaborationRepository, OrganizationRepository organizationRepository, ZohoSignedDocumentRepository zohoSignedDocumentRepository, OfflinePartnerInviteRepository offlinePartnerInviteRepository, ConfigurationRepository configurationRepository) {
        this.organizationCollaborationRepository = organizationCollaborationRepository;
        this.organizationRepository = organizationRepository;
        this.zohoSignedDocumentRepository = zohoSignedDocumentRepository;
        this.offlinePartnerInviteRepository = offlinePartnerInviteRepository;
        this.configurationRepository = configurationRepository;
    }

    @Transactional
    public void signDocument(Long organizationCollaborationId, MultipartFile file) {
        var optionalOrganizationCollaboration = organizationCollaborationRepository.findById(organizationCollaborationId);
        if (optionalOrganizationCollaboration.isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH09, organizationCollaborationId);
        }
        var organizationCollaboration = optionalOrganizationCollaboration.get();
        String accessToken = getAccessToken();
        var receiverOrganizationId = organizationCollaboration.getReceiverOrganizationId();
        var optionalReceiverOrganization = organizationRepository.findById(receiverOrganizationId);
        if (optionalReceiverOrganization.isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH08, receiverOrganizationId);
        }
        var receiverOrganization = optionalReceiverOrganization.get();
        uploadDocument(accessToken, receiverOrganization.getPrimaryEmail(), receiverOrganization.getName(), file, organizationCollaborationId, null,false, null, false);
    }

    @Cacheable(value = "zohoAccessToken", key = "'token'", unless = "#result == null")
    public String getAccessToken() {
        String url = "https://accounts.zoho.in/oauth/v2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String clientId = configurationRepository.findByKey("ZOHO_CLIENT_ID").get().getValue();
        String refreshToken = configurationRepository.findByKey("ZOHO_REFRESH_TOKEN").get().getValue();
        String clientSecret = configurationRepository.findByKey("ZOHO_CLIENT_SECRET").get().getValue();
        String body = String.format("refresh_token=%s" +
                "&client_id=%s" +
                "&client_secret=%s" +
                "&grant_type=refresh_token", refreshToken, clientId, clientSecret);

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody.containsKey("error")) {
            throw new RuntimeException(responseBody.get("error").toString());
        }
        return responseBody.containsKey("access_token")
                ? Objects.toString(responseBody.get("access_token"), null)
                : null;
    }

    private void uploadDocument(String accessToken, String email, String name, MultipartFile file,
                                Long collaborationId,String offlinePartnerCode, boolean sender,
                                String recipientDocumentId, boolean offlinePartner) {
        try {
            String url = "https://sign.zoho.in/api/v1/requests";
            String authToken = String.format("Zoho-oauthtoken %s", accessToken);

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            String data = "{\"requests\":{\"request_name\":\"Sharkdom NDA\",\"actions\":[{\"recipient_name\":\"" + name +
                    "\",\"recipient_email\":\"" + email +
                    "\",\"action_type\":\"SIGN\",\"signing_order\":0,\"verify_recipient\":true,\"verification_type\":\"EMAIL\"," +
                    "\"verification_code\":\"ABCDEF\",\"private_notes\":\"Sign the doc\"}],\"expiration_days\":10," +
                    "\"is_sequential\":true,\"email_reminders\":true,\"reminder_period\":4}}";

            // Add the data part
            body.add("data", data);

            // Add the file part
            Resource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send request
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                Map<String, Object> responseBody = responseEntity.getBody();

                Map<String, Object> requests = (Map<String, Object>) responseBody.get("requests");

                String requestId = requests.get("request_id").toString();

                List<Map<String, Object>> actions = (List<Map<String, Object>>) requests.get("actions");
                String actionId = actions.get(0).get("action_id").toString();

                List<Map<String, Object>> documentIds = (List<Map<String, Object>>) requests.get("document_ids");
                String documentId = documentIds.get(0).get("document_id").toString();
                sendSignRequest(accessToken, actionId, documentId, requestId, collaborationId, offlinePartnerCode ,sender, recipientDocumentId, offlinePartner);
            } else {
                throw new SharkdomException(ErrorMessages.SH120, responseEntity.getBody());
            }
        } catch (Exception e) {
            throw new SharkdomException(ErrorMessages.SH123, e.getMessage());
        }
    }

    private void sendSignRequest(String accessToken, String actionId, String documentId, String requestId,
                                 Long collaborationId,String offlinePartnerCode ,boolean sender,
                                 String recipientDocumentId, boolean offlinePartner) {
        try {
            String url = String.format("https://sign.zoho.in/api/v1/requests/%s/submit", requestId);
            String authToken = String.format("Zoho-oauthtoken %s", accessToken);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String xPosition = sender ? "20.499843" : "68.499843";
            String data = String.format("""
                    {
                         "requests": {
                             "actions": [
                                 {
                                     "action_id": "%s",
                                     "action_type": "SIGN",
                                     "fields": [
                                         {
                                             "field_type_name": "Signature",
                                             "action_id": "%s",
                                             "document_id": "%s",
                                             "field_name": "Signature",
                                             "width": "22.045264",
                                             "height": "2.461140",
                                             "x_value": "%s",
                                             "y_value": "84.132124",
                                             "page_no": 0,
                                             "field_category": "image",
                                             "field_label": "Signature"
                                         }
                                     ]
                                 }
                             ]
                         }
                     }
                    """, actionId, actionId, documentId, xPosition);

            HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new SharkdomException(ErrorMessages.SH120, responseEntity.getBody());
            }

            if (sender && recipientDocumentId != null) {
                // Find the document by recipient document ID
                ZohoSignedDocumentEntity existingDocument = zohoSignedDocumentRepository
                        .findByRecipientDocumentId(recipientDocumentId);

                if (existingDocument != null) {
                    // Update the existing entity with sender information
                    existingDocument.setSenderDocumentId(documentId);
                    existingDocument.setSenderRequestId(requestId);
                    existingDocument.setSenderActionId(actionId);
                    zohoSignedDocumentRepository.save(existingDocument);
                } else {
                    throw new SharkdomException(ErrorMessages.SH121, recipientDocumentId);
                }
            } else if (!sender) {
                // This is a new document for the recipient
                var zohoSignedDocumentEntity = ZohoSignedDocumentEntity.builder()
                        .recipientDocumentId(documentId)
                        .status(MouStatus.PENDING_RECEIVER)
                        .recipientRequestId(requestId)
                        .recipientActionId(actionId)
                        .build();
                if (offlinePartner) {
                    zohoSignedDocumentEntity.setOfflinePartnerId(collaborationId);
                    zohoSignedDocumentEntity.setOfflinePartnerCode(offlinePartnerCode);
                } else {
                    zohoSignedDocumentEntity.setOrganizationCollaborationId(collaborationId);
                }
                zohoSignedDocumentRepository.save(zohoSignedDocumentEntity);
            } else {
                throw new SharkdomException(ErrorMessages.SH122);
            }
        } catch (Exception e) {
            throw new SharkdomException(ErrorMessages.SH123, e.getMessage());
        }
    }

    public void handleCallback(Map<Object, Object> request) {
        try {
            // Extracting values from the request map
            Map<String, Object> requests = (Map<String, Object>) request.get("requests");
            Map<String, Object> notifications = (Map<String, Object>) request.get("notifications");

            String requestId = String.valueOf(requests.get("request_id"));
            String requestStatus = String.valueOf(requests.get("request_status"));
            String performedByEmail = String.valueOf(notifications.get("performed_by_email"));
            String ipAddress = String.valueOf(notifications.get("ip_address"));

            if ("completed".equals(requestStatus)) {
                ZohoSignedDocumentEntity zohoSignedDocumentEntity = zohoSignedDocumentRepository.findByRecipientRequestId(requestId);
                if (zohoSignedDocumentEntity != null) {
                    zohoSignedDocumentEntity.setStatus(MouStatus.PENDING_SENDER);
                    zohoSignedDocumentEntity.setRecipientEmail(performedByEmail);
                    zohoSignedDocumentEntity.setRecipientIp(ipAddress);
                    zohoSignedDocumentEntity.setRecipientSignedAt(new Date());
                    zohoSignedDocumentRepository.save(zohoSignedDocumentEntity);

                    byte[] certificateBytes = downloadCompletionCertificate(requestId);
                    boolean isOfflinePartner = zohoSignedDocumentEntity.getOfflinePartnerId() != null;
                    Long collaborationId = isOfflinePartner ?
                            zohoSignedDocumentEntity.getOfflinePartnerId() :
                            zohoSignedDocumentEntity.getOrganizationCollaborationId();

                    String senderEmail;
                    String senderName;

                    if (isOfflinePartner) {
                        // Handle offline partner case
                        var offlinePartner = offlinePartnerInviteRepository
                                .findById(collaborationId)
                                .orElseThrow(() -> new SharkdomException(ErrorMessages.SH111, collaborationId));

                        senderEmail = offlinePartner.getEmail();
                        senderName = offlinePartner.getPartnerName();
                    } else {
                        // Handle organization collaboration case
                        var organizationCollaboration = organizationCollaborationRepository
                                .findById(collaborationId)
                                .orElseThrow(() -> new SharkdomException(ErrorMessages.SH09, collaborationId));

                        var senderOrganizationId = organizationCollaboration.getSenderOrganizationId();
                        var senderOrganization = organizationRepository
                                .findById(senderOrganizationId)
                                .orElseThrow(() -> new SharkdomException(ErrorMessages.SH08, senderOrganizationId));

                        senderEmail = senderOrganization.getPrimaryEmail();
                        senderName = senderOrganization.getName();
                    }

                    String accessToken = getAccessToken();

                    // Create a MultipartFile from the certificate bytes
                    MultipartFile certificateFile = new ByteArrayMultipartFile(
                            "certificate.pdf",
                            "certificate.pdf",
                            "application/pdf",
                            certificateBytes
                    );

                    // Get the recipientDocumentId to pass to uploadDocument
                    String recipientDocumentId = zohoSignedDocumentEntity.getRecipientDocumentId();

                    uploadDocument(
                            accessToken,
                            senderEmail,
                            senderName,
                            certificateFile,
                            zohoSignedDocumentEntity.getOrganizationCollaborationId(),
                            null,
                            true,
                            recipientDocumentId,
                            isOfflinePartner
                    );
                } else {
                    ZohoSignedDocumentEntity senderEntity = zohoSignedDocumentRepository.findBySenderRequestId(requestId);
                    if (senderEntity != null) {
                        senderEntity.setStatus(MouStatus.ACTIVE);
                        senderEntity.setSenderEmail(performedByEmail);
                        senderEntity.setSenderIp(ipAddress);
                        senderEntity.setSenderSignedAt(new Date());
                        zohoSignedDocumentRepository.save(senderEntity);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting data: " + e.getMessage());
        }
    }

    private byte[] downloadCompletionCertificate(String requestId) {
        try {
            String url = String.format("https://sign.zoho.in/api/v1/requests/%s/pdf", requestId);
            String accessToken = getAccessToken();
            String authToken = String.format("Zoho-oauthtoken %s", accessToken);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.set("Accept", "application/pdf");

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Use RestTemplate to make the request but get the response as byte[]
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    byte[].class
            );
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new SharkdomException(ErrorMessages.SH124);
            } else {
                responseEntity.getBody();
            }

            return responseEntity.getBody();
        } catch (Exception e) {
            throw new SharkdomException(ErrorMessages.SH125, e.getMessage());
        }
    }

    public void signOfflinePartnerDocument(Long organizationId, Long offlinePartnerId, String offlinePartnerCode,MultipartFile file) {
        var optionalOrganization = organizationRepository.findById(organizationId);
        if (optionalOrganization.isEmpty()) {
            throw new ServiceException(ErrorMessages.SH08, organizationId);
        }
        String accessToken = getAccessToken();
        var organization = optionalOrganization.get();
        uploadDocument(accessToken, organization.getPrimaryEmail(), organization.getName(), file, offlinePartnerId,offlinePartnerCode ,false, null, true);

    }

    public List<ZohoSignedDocumentEntity> getOfflineZohoDocuments(Long id) {
        return zohoSignedDocumentRepository.findAllByOfflinePartnerId(id);
    }

}

