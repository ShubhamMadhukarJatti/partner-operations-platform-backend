package com.sharkdom.service.mou;

import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.Flag;
import com.sharkdom.constants.MouStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.mou.MouHistory;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.EnvelopeEntity;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.entity.organizationcollaboration.PartnershipMouVersion;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.repository.mou.MouHistoryRepository;
import com.sharkdom.repository.mou.SignedDocumentRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.organizationcollaboration.EnvelopeRepository;
import com.sharkdom.repository.organizationcollaboration.PartnershipMouVersionRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.notification.NotificationService;
import com.sharkdom.util.AzureStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MouPdfGenerator {
    private final SignedDocumentRepository signedDocumentRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final PartnershipMouVersionRepository partnershipMouVersionRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final WebSocketHandler webSocketHandler;
    private final AzureStorageService azureStorageService;
    private final MouHistoryRepository mouHistoryRepository;
    private final EnvelopeRepository envelopeRepository;
    @Value("${env}")
    private String env;

    public MouPdfGenerator(SignedDocumentRepository signedDocumentRepository, OrganizationRepository organizationRepository, OrganizationUserMappingRepository organizationUserMappingRepository, PartnershipMouVersionRepository partnershipMouVersionRepository, EmailService emailService, NotificationService notificationService, WebSocketHandler webSocketHandler, AzureStorageService azureStorageService, MouHistoryRepository mouHistoryRepository, EnvelopeRepository envelopeRepository) {
        this.signedDocumentRepository = signedDocumentRepository;
        this.organizationRepository = organizationRepository;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.partnershipMouVersionRepository = partnershipMouVersionRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.webSocketHandler = webSocketHandler;
        this.azureStorageService = azureStorageService;
        this.mouHistoryRepository = mouHistoryRepository;
        this.envelopeRepository = envelopeRepository;
    }

    public void generateMou(OrganizationCollaboration organizationCollaboration) {
        Organization receiverOrganization = organizationRepository.findById(organizationCollaboration.getReceiverOrganizationId()).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH22, organizationCollaboration.getReceiverOrganizationId()));
        Organization senderOrganization = organizationRepository.findById(organizationCollaboration.getSenderOrganizationId()).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH22, organizationCollaboration.getSenderOrganizationId()));
        PartnershipMouVersion partnershipMouVersion = organizationCollaboration.getPartnershipMouVersions().get(organizationCollaboration.getPartnershipMouVersions().size() - 1);
        Optional<OrganizationUserMappingResponse> senderOrganizationAdmin = organizationUserMappingRepository.findByOrganizationIdAndRole(senderOrganization.getId(), OrgUserRole.ADMIN).stream().findFirst();
        if (senderOrganizationAdmin.isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH104);
        }
        Optional<OrganizationUserMappingResponse> receiverOrganizationAdmin = organizationUserMappingRepository.findByOrganizationIdAndRole(receiverOrganization.getId(), OrgUserRole.ADMIN).stream().findFirst();
        if (receiverOrganizationAdmin.isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH105);
        }
        String fileName = generateMouPdf(receiverOrganization, senderOrganization, partnershipMouVersion, senderOrganizationAdmin.get(), receiverOrganizationAdmin.get());
        if (fileName == null) {
            throw new RuntimeException("Unable to generate MOU");
        }
        uploadFile(fileName, organizationCollaboration.getId(), organizationCollaboration.getReceiverOrganizationId());
        partnershipMouVersion.setStatus(MouStatus.PENDING_RECEIVER);
        partnershipMouVersionRepository.save(partnershipMouVersion);
        String docUrl;
        if (env.equals("dev")) {
            docUrl = "https://dev.sharkdom.com/mou/" + organizationCollaboration.getId() + "/sign";
        } else {
            docUrl = "https://sharkdom.com/mou/" + organizationCollaboration.getId() + "/sign";
        }
        emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                .organizationIds(List.of(receiverOrganization.getId()))
                .organizationName(receiverOrganization.getName())
                .senderOrganizationName(senderOrganization.getName())
                .templateCode("Signer_Receiver")
                .docUrl(docUrl).build(), null, organizationCollaboration.getId(), 0L);
        Notification notification = Notification.builder()
                .subject("MOU Signing Required")
                .body(String.format(" %s has sent you a document to review and sign. Please check your email for the Memorandum of Understanding (MOU) and follow the instructions to complete the signing process.", senderOrganization.getName()))
                .forWeb(true)
                .organizationId(receiverOrganization.getId())
                .build();
        webSocketHandler.sendMessageToUser(receiverOrganization.getId(), notification);

        notificationService.create(notification);
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

    private String generateMouPdf(Organization receiverOrganization, Organization senderOrganization, PartnershipMouVersion partnershipMouVersion, OrganizationUserMappingResponse senderOrganizationAdmin, OrganizationUserMappingResponse receiverOrganizationAdmin) {
        LocalDate today = LocalDate.now();
        DateTime date = new DateTime();
        LocalDate sixMonthsDate = today.plusMonths(6);
        String senderOrganizationName = senderOrganization.getName().replace(".", "");
        String receiverOrganizationName = receiverOrganization.getName().replace(".", "");
        String fileName = senderOrganizationName + "X" + receiverOrganizationName + "_" + today + "_" + date.getSecondOfDay();

        // Save the file in the /home directory which is writable in Azure App Service
        //String filePath = fileName + ".pdf";
        String filePath = Paths.get("/home", fileName + ".pdf").toString();
        Document document = new Document(PageSize.A4, 56f, 56f, 36f, 36f);
        try {
            PdfWriter.getInstance(document,
                    new FileOutputStream(filePath));
            Font footerFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Color.BLUE);
            HeaderFooter footer = new HeaderFooter(new Phrase("https://sharkdom.com", footerFont), false);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setBorderWidthBottom(0);
            document.setFooter(footer);
            document.open();
            document.addTitle("Memorandum of Understanding");
            Font mainHeadingFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 20, Font.BOLD);
            Paragraph paragraph = new Paragraph("\n \n Memorandum of Understanding(MOU) \n \n", mainHeadingFont);
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(paragraph);
            Font secondHeadingFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy");
            String formattedDate = today.format(formatter);
            document.add(new Paragraph("This Memorandum of Understanding (MOU) is made and entered into on " + formattedDate + " between " + senderOrganization.getName() + " & " + receiverOrganization.getName() + ".\n \n "));
            document.add(new Paragraph("Purpose and Scope\n\n", secondHeadingFont));

            document.add(new Paragraph("The purpose of this MOU is to partner " + senderOrganization.getName() + ", " + senderOrganization.getBriefDescription() + " with " + receiverOrganization.getName() + ", " + receiverOrganization.getBriefDescription() + ".\n \n\n"));
            document.add(new Paragraph("Responsibilities \n\n", secondHeadingFont));
            document.add(new Paragraph(senderOrganization.getName() + ", " + partnershipMouVersion.getSenderBenefits().get(0).getBenefit() + ". " + receiverOrganization.getName() + ", " + partnershipMouVersion.getReceiverBenefits().get(0).getBenefit() + ".\n\n\n"));

            document.add(new Paragraph("Timeline \n\n", secondHeadingFont));
            document.add(new Paragraph("This MOU is effective for 6 months, starting from " + StringUtils.capitalize(today.getMonth().toString().toLowerCase()) + " " + today.getYear() + " to " + StringUtils.capitalize(sixMonthsDate.getMonth().toString().toLowerCase()) + " " + sixMonthsDate.getYear() + ". \n\n\n"));

            document.add(new Paragraph("Communication\n\n", secondHeadingFont));
            document.add(new Paragraph("Communication would be held by " + senderOrganizationAdmin.getUser().getName() + " of " + senderOrganization.getName() + " via " + senderOrganization.getPrimaryEmail() + " and " + receiverOrganizationAdmin.getUser().getName() + " of " + receiverOrganization.getName() + " or any other member assigned by Team " + receiverOrganization.getName() + ". \n\n\n"));

            document.add(new Paragraph("Termination\n\n", secondHeadingFont));
            document.add(new Paragraph(" In case any of the above two parties mentioned have a dispute arising due to " +
                    "the quality of services provided or using other parties' brand name for self-goal " +
                    "without notifying the party first, or sharing a malicious link, the partnership can be brought to " +
                    "termination after notifying the other party. The termination can be done by either party by sending an email after a meet. \n\n\n"));

            document.add(new Paragraph("Confidentiality\n\n", secondHeadingFont));
            document.add(new Paragraph("The information regarding this agreement will not be shared outside of the " + senderOrganization.getName() + " team and " + receiverOrganization.getName() + " team. \n\n\n "));

            document.add(new Paragraph("Governing Law\n\n", secondHeadingFont));
            document.add(new Paragraph("Any disputes arising from or related to this MOU shall be resolved by law in case the Mediator, which in this case is 'SharkDom,' " +
                    "is unable to resolve the issue as agreed by both parties. " +
                    "This MOU contains the entire understanding of the parties and supersedes all prior negotiations, understandings," +
                    " and agreements between them, whether oral or written before the initiation of the partnership inside the platform." +
                    " This MOU may only be modified in writing and signed by both parties \n\n\n"));

            document.add(new Paragraph("Contact Information\n\n", secondHeadingFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            table.getDefaultCell().setBorder(0);
            PdfPCell senderCell = getCell(senderOrganizationAdmin, senderOrganization);
            table.addCell(senderCell);

            PdfPCell receiverCell = getCell(receiverOrganizationAdmin, receiverOrganization);
            table.addCell(receiverCell);

            document.add(table);
            document.close();
        } catch (FileNotFoundException e) {
            log.error("unable to create file");
            return null;
        }
        document.close();
        return fileName;
    }

    private PdfPCell getCell(OrganizationUserMappingResponse organizationAdmin, Organization organization) {
        String senderDesignation = organizationAdmin.getOrganizationUserMapping().getDesignation();
        if (senderDesignation == null) {
            senderDesignation = "Member";
        }
        PdfPCell cellX = new PdfPCell(new Paragraph(organizationAdmin.getUser().getName() + "\n" + senderDesignation + " " + organization.getName() + "\nEmail: " + organization.getPrimaryEmail() + "\nWebsite: " + organization.getWebsite()));
        cellX.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cellX.setBorder(0);
        return cellX;
    }


    public void uploadFile(String fileName, Long orgCollabId, Long receiverOrganizationId) {
        Path filePath = Paths.get("/home", fileName + ".pdf");
        //Path filePath = Paths.get(fileName + ".pdf");
        try {
            byte[] content = Files.readAllBytes(filePath);

            // Convert byte array to InputStream for upload
            try (InputStream inputStream = new ByteArrayInputStream(content)) {
                // Call azureStorageService with inputStream
                var pdfPath = "/" + env + "/" + orgCollabId + "/" + receiverOrganizationId + "/" + fileName + ".pdf";
                var pdfLink = azureStorageService.uploadFile(inputStream, pdfPath);
                var envelopeId = UUID.randomUUID().toString();
                var mouHistory = MouHistory.builder()
                        .organizationCollaborationId(orgCollabId)
                        .organizationId(receiverOrganizationId)
                        .pdfUrl(pdfPath)
                        .type(Flag.RECEIVER)
                        .signed(false)
                        .envelopeId(envelopeId).build();
                mouHistoryRepository.save(mouHistory);
                var envelope = EnvelopeEntity.builder().envelopeId(envelopeId)
                        .subject(fileName)
                        .signedByReceiver(false)
                        .dateCreated(LocalDate.now())
                        .signedBySender(false)
                        .statusDate(LocalDate.now())
                        .holder(organizationRepository.findNameById(receiverOrganizationId))
                        .status("NOT_STARTED")
                        .build();
                envelopeRepository.save(envelope);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
