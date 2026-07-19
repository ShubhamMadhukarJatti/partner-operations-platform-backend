package com.sharkdom.mypartner.service;

import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.mypartner.dto.PartnerStatusResponseDTO;
import com.sharkdom.mypartner.dto.SendPartnerCredentialDTO;
import com.sharkdom.mypartner.dto.SendPartnerTraningCredentialDTO;
import com.sharkdom.mypartner.entity.MyPartnerSendCredential;
import com.sharkdom.mypartner.entity.MyPartnerSendTrainingCredential;
import com.sharkdom.mypartner.repository.SendMyPartnerCredentialRepository;
import com.sharkdom.mypartner.repository.SendMyPartnerTraningCredentialRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.organization.OrganizationUserMappingService;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MyPartnerService {

    @Autowired
    private OrganizationCollaborationRepository organizationCollaborationRepository;

    @Autowired
    private OrganizationUserMappingService organizationUserMappingService;

    @Autowired
    private SendMyPartnerCredentialRepository sendMyPartnerCredentialRepository;

    @Autowired
    private SendMyPartnerTraningCredentialRepository sendMyPartnerTraningCredentialRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Value("${message.encryption-key}")
    private String messageEncryptionKey;

    public void sendPartnerTrainingCredential(SendPartnerTraningCredentialDTO sendPartnerCredentialDTO) throws Exception {
        log.info("Start sending credential for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());

        if (sendPartnerCredentialDTO.getPartnerId() == null ||
                sendPartnerCredentialDTO.getUsername() == null ||
                sendPartnerCredentialDTO.getPassword() == null ||
                sendPartnerCredentialDTO.getUrl() == null) {
            log.error("Invalid input data for sending credentials: {}", sendPartnerCredentialDTO);
            throw new IllegalArgumentException("PartnerId, Username, Password, and URL are required!");
        }

        String currentUserId = Util.getOrgIdFromToken().toString();
        log.info("Request initiated by userId: {}", currentUserId);

        // Get Partner ADMIN email
        List<OrganizationUserMappingResponse> userMappingResponses =
                organizationUserMappingService.findAllByOrganizationId(Long.valueOf(sendPartnerCredentialDTO.getPartnerId()));

        log.info("Found {} user mappings for partnerId {}", userMappingResponses.size(), sendPartnerCredentialDTO.getPartnerId());

        String receiverEmail = null;
        for (OrganizationUserMappingResponse userMappingResponse : userMappingResponses) {
            if (OrgUserRole.ADMIN.equals(userMappingResponse.getOrganizationUserMapping().getRole())) {
                receiverEmail = userMappingResponse.getUser().getEmail();
                log.info("Found partner ADMIN email: {}", receiverEmail);
                break;
            }
        }


        if (receiverEmail == null) {
            log.error("No ADMIN user found for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());
            throw new IllegalArgumentException("Partner does not have an ADMIN user to receive credentials.");
        }

        // Send credentials via email
        try {
            emailService.sendPartnerCredentialByEmail(
                    receiverEmail,
                    sendPartnerCredentialDTO.getUsername(),
                    sendPartnerCredentialDTO.getPassword(),
                    sendPartnerCredentialDTO.getUrl(),
                    "send_Credentials_Partner"
            );
            log.info("Email sent successfully to {}", receiverEmail);
        } catch (Exception e) {
            log.error("Failed to send email to partnerId {}: {}", sendPartnerCredentialDTO.getPartnerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email to partner.");
        }

        // Save encrypted credentials to DB
        try {
            MyPartnerSendTrainingCredential myPartnerSendCredential = new MyPartnerSendTrainingCredential();
            myPartnerSendCredential.setUsername(sendPartnerCredentialDTO.getUsername());
            myPartnerSendCredential.setPassword(
                    Util.encryptForDatabase(sendPartnerCredentialDTO.getPassword(), messageEncryptionKey)
            );
            myPartnerSendCredential.setUrl(sendPartnerCredentialDTO.getUrl());
            myPartnerSendCredential.setReceiverId(sendPartnerCredentialDTO.getPartnerId());
            myPartnerSendCredential.setSenderId(Long.valueOf(currentUserId));

            MyPartnerSendTrainingCredential saved = sendMyPartnerTraningCredentialRepository.save(myPartnerSendCredential);
            log.info("Credential saved successfully with id: {}", saved.getId());
        } catch (Exception e) {
            log.error("Error saving credentials in DB for partnerId {}: {}", sendPartnerCredentialDTO.getPartnerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save partner credentials.");
        }

        log.info("Completed sending credential process for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());
    }


    public void sendCredential(SendPartnerCredentialDTO sendPartnerCredentialDTO) throws Exception {
        log.info("Start sending credential for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());

        if (sendPartnerCredentialDTO.getPartnerId() == null ||
                sendPartnerCredentialDTO.getUsername() == null ||
                sendPartnerCredentialDTO.getPassword() == null ||
                sendPartnerCredentialDTO.getUrl() == null) {
            log.error("Invalid input data for sending credentials: {}", sendPartnerCredentialDTO);
            throw new IllegalArgumentException("PartnerId, Username, Password, and URL are required!");
        }

        String currentUserId = Util.getUserFromToken();
        log.info("Request initiated by userId: {}", currentUserId);

        // Get Partner ADMIN email
        List<OrganizationUserMappingResponse> userMappingResponses =
                organizationUserMappingService.findAllByOrganizationId(Long.valueOf(sendPartnerCredentialDTO.getPartnerId()));

        log.info("Found {} user mappings for partnerId {}", userMappingResponses.size(), sendPartnerCredentialDTO.getPartnerId());

        String receiverEmail = null;
        for (OrganizationUserMappingResponse userMappingResponse : userMappingResponses) {
            if (OrgUserRole.ADMIN.equals(userMappingResponse.getOrganizationUserMapping().getRole())) {
                receiverEmail = userMappingResponse.getUser().getEmail();
                log.info("Found partner ADMIN email: {}", receiverEmail);
                break;
            }
        }


        if (receiverEmail == null) {
            log.error("No ADMIN user found for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());
            throw new IllegalArgumentException("Partner does not have an ADMIN user to receive credentials.");
        }

        // Send credentials via email
        try {
            emailService.sendPartnerCredentialByEmail(
                    receiverEmail,
                    sendPartnerCredentialDTO.getUsername(),
                    sendPartnerCredentialDTO.getPassword(),
                    sendPartnerCredentialDTO.getUrl(),
                    "send_Credentials_Partner"
            );
            log.info("Email sent successfully to {}", receiverEmail);
        } catch (Exception e) {
            log.error("Failed to send email to partnerId {}: {}", sendPartnerCredentialDTO.getPartnerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email to partner.");
        }

        // Save encrypted credentials to DB
        try {
            MyPartnerSendCredential myPartnerSendCredential = new MyPartnerSendCredential();
            myPartnerSendCredential.setUsername(sendPartnerCredentialDTO.getUsername());
            myPartnerSendCredential.setPassword(
                    Util.encryptForDatabase(sendPartnerCredentialDTO.getPassword(), messageEncryptionKey)
            );
            myPartnerSendCredential.setUrl(sendPartnerCredentialDTO.getUrl());
            myPartnerSendCredential.setReceiverId(sendPartnerCredentialDTO.getPartnerId());
            myPartnerSendCredential.setSenderId(currentUserId);

            MyPartnerSendCredential saved = sendMyPartnerCredentialRepository.save(myPartnerSendCredential);
            log.info("Credential saved successfully with id: {}", saved.getId());
        } catch (Exception e) {
            log.error("Error saving credentials in DB for partnerId {}: {}", sendPartnerCredentialDTO.getPartnerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save partner credentials.");
        }

        log.info("Completed sending credential process for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());
    }


    public boolean isTrainingCredentialAlreadySent(Long partnerId) {
        log.info("Checking if credentials already sent for partnerId: {}", partnerId);

        boolean exists;

        // Basic check (by receiverId)
        exists = sendMyPartnerTraningCredentialRepository
                .existsBySenderIdAndReceiverId(Util.getOrgIdFromToken(),partnerId);

        log.info("Credential already sent status for partnerId {}: {}", partnerId, exists);

        return exists;
    }

    public PartnerStatusResponseDTO getPartnerStatus(String partnerId) {

        log.info("Fetching partner status for partnerId: {}", partnerId);

        // Static response for now
        return new PartnerStatusResponseDTO(
                true,   // mouUploaded
                true,   // signedByY
                true,   // partnershipEnabled
                false   // trainingCredentialSent
        );
    }
}

