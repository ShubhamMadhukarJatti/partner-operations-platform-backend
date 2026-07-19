package com.sharkdom.service.mou;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.repository.mou.SignedDocumentRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.organizationcollaboration.PartnershipMouVersionRepository;
import com.sharkdom.service.notification.NotificationService;
import com.sharkdom.util.aws.service.AmazonS3Service;
import com.sharkdom.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class MouService {
    private final MouPdfGenerator mouPdfGenerator;
    private final SignedDocumentRepository signedDocumentRepository;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final EmailService emailService;
    private final PartnershipMouVersionRepository partnershipMouVersionRepository;
    private final AmazonS3Service amazonS3Service;
    private final NotificationService notificationService;
    private final WebSocketHandler webSocketHandler;

    public MouService(MouPdfGenerator mouPdfGenerator, SignedDocumentRepository signedDocumentRepository, OrganizationCollaborationRepository organizationCollaborationRepository, OrganizationRepository organizationRepository, OrganizationUserMappingRepository organizationUserMappingRepository, EmailService emailService, PartnershipMouVersionRepository partnershipMouVersionRepository, AmazonS3Service amazonS3Service, NotificationService notificationService, WebSocketHandler webSocketHandler) {
        this.mouPdfGenerator = mouPdfGenerator;
        this.signedDocumentRepository = signedDocumentRepository;
        this.organizationCollaborationRepository = organizationCollaborationRepository;
        this.organizationRepository = organizationRepository;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.emailService = emailService;
        this.partnershipMouVersionRepository = partnershipMouVersionRepository;
        this.amazonS3Service = amazonS3Service;
        this.notificationService = notificationService;
        this.webSocketHandler = webSocketHandler;
    }

  /*  public void updateMou(Map dataMap) {
        Map<String, Object> data = (Map<String, Object>) dataMap.get("data");
        Map<String, Object> esign = (Map<String, Object>) data.get("esign");
        String documentId = (String) esign.get("documentId");
        String requestId = (String) esign.get("id");
        String status = (String) esign.get("status");
        if ("sign_complete".equalsIgnoreCase(status)) {
            SignedDocument signedDocument = signedDocumentRepository.findByDocumentId(documentId);
            if (signedDocument.getStatus().equalsIgnoreCase(MouStatus.PENDING_RECEIVER.name())) {
                Optional<OrganizationCollaboration> organizationCollaboration = organizationCollaborationRepository.findById(signedDocument.getOrganizationCollaborationId());
                if (organizationCollaboration.isPresent()) {
                    Organization receiverOrganization = organizationRepository.findById(organizationCollaboration.get().getReceiverOrganizationId()).orElseThrow(() -> new ResourceNotFoundException("Organization does not exist for id" + organizationCollaboration.get().getReceiverOrganizationId()));
                    Organization senderOrganization = organizationRepository.findById(organizationCollaboration.get().getSenderOrganizationId()).orElseThrow(() -> new ResourceNotFoundException("Organization does not exist for id" + organizationCollaboration.get().getSenderOrganizationId()));
                    PartnershipMouVersion partnershipMouVersion = organizationCollaboration.get().getPartnershipMouVersions().get(organizationCollaboration.get().getPartnershipMouVersions().size() - 1);
                    OrganizationUserMappingResponse senderOrganizationAdmin = organizationUserMappingRepository.findByOrganizationIdAndRole(senderOrganization.getId(), OrgUserRole.ADMIN).get(0);
                    String fileName = mouPdfGenerator.downloadFile(requestId, senderOrganization, receiverOrganization);
                    if (fileName != null) {
                        SignedDocument signedDocumentAfter = mouPdfGenerator.uploadFile(fileName, organizationCollaboration.get().getId(), partnershipMouVersion.getId(), MouStatus.PENDING_SENDER.name());
                        if (signedDocumentAfter == null) {
                            throw new RuntimeException("Unable to save MOU");
                        }
                        String url = mouPdfGenerator.addSignature(signedDocumentAfter.getDocumentId(), senderOrganizationAdmin.getUser().getName(), "bottom-left");
                        if (url == null) {
                            throw new RuntimeException("Unable to get url");
                        }
                        partnershipMouVersion.setStatus(MouStatus.PENDING_SENDER);
                        partnershipMouVersion.setReceiverSignedOn(DateTime.now().toDate());
                        partnershipMouVersionRepository.save(partnershipMouVersion);
                        emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                                .organizationIds(List.of(senderOrganization.getId()))
                                .templateCode("Signer_Sender")
                                .docUrl(url).build(), null, organizationCollaboration.get().getId(), 1L);

                        File file = new File(fileName + ".pdf");
                        if (file.exists()) {
                            if (file.delete()) {
                                log.info("File deleted successfully.");
                            } else {
                                log.error("Failed to delete the file.");
                            }
                        } else {
                            log.error("File not found.");
                        }
                    }
                }

            } else if (signedDocument.getStatus().equalsIgnoreCase(MouStatus.PENDING_SENDER.name())) {
                Optional<OrganizationCollaboration> organizationCollaboration = organizationCollaborationRepository.findById(signedDocument.getOrganizationCollaborationId());
                if (organizationCollaboration.isPresent()) {
                    Organization receiverOrganization = organizationRepository.findById(organizationCollaboration.get().getReceiverOrganizationId()).orElseThrow(() -> new ResourceNotFoundException("Organization does not exist for id" + organizationCollaboration.get().getReceiverOrganizationId()));
                    Organization senderOrganization = organizationRepository.findById(organizationCollaboration.get().getSenderOrganizationId()).orElseThrow(() -> new ResourceNotFoundException("Organization does not exist for id" + organizationCollaboration.get().getSenderOrganizationId()));
                    PartnershipMouVersion partnershipMouVersion = organizationCollaboration.get().getPartnershipMouVersions().get(organizationCollaboration.get().getPartnershipMouVersions().size() - 1);
                    String fileName = mouPdfGenerator.downloadFile(requestId, senderOrganization, receiverOrganization);
                    if (fileName != null) {
                        partnershipMouVersion.setSenderSignedOn(DateTime.now().toDate());
                        partnershipMouVersion.setStatus(MouStatus.ACTIVE);
                        uploadToS3(fileName);
                        partnershipMouVersion.setFilePath("sharkdom.co.in/mous/" + fileName + ".pdf");
                        partnershipMouVersionRepository.save(partnershipMouVersion);
                        emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                                .organizationIds(List.of(senderOrganization.getId(), receiverOrganization.getId()))
                                .templateCode("Signer_Complete").build(), null, organizationCollaboration.get().getId(), 1L);

                        notificationService.create(createProposalAcceptNotification(senderOrganization.getId()));
                        notificationService.create(createProposalAcceptNotification(receiverOrganization.getId()));
                        File filePath = new File(fileName + ".pdf");

                        if (filePath.exists()) {
                            if (filePath.delete()) {
                                log.info("File deleted successfully.");
                            } else {
                                log.error("Failed to delete the file.");
                            }
                        } else {
                            log.error("File not found.");
                        }
                    }
                }
            }
        }

    }*/

    private void uploadToS3(String fileName) {
        File file = new File(fileName + ".pdf");
        try {
            var s3Client = amazonS3Service.getS3Instance();
            s3Client.putObject(new PutObjectRequest("sharkdom.co.in/mous", fileName, file));
            log.info("uploaded to s3");
        } catch (Exception e) {
            log.error("exception occurred while uploading");
        }
    }

    private Notification createProposalAcceptNotification(Long organizationId) {
        var notification = Notification.builder()
                .subject("Proposal Accepted")
                .body("Congratulations! Your proposal has been accepted. We are excited to move forward with this partnership. Please check your email for more details.")
                .forWeb(true)
                .organizationId(organizationId)
                .build();
        webSocketHandler.sendMessageToUser(organizationId, notification);
        return notification;

    }
}
