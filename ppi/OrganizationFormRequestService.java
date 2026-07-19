package com.sharkdom.service.ppi;

import com.sharkdom.dto.FormStatusResponseDto;
import com.sharkdom.dto.OrganizationFormRequestCreateDto;
import com.sharkdom.emailOutreach.dto.SendMailRequest;
import com.sharkdom.emailOutreach.dto.SendMailResponse;
import com.sharkdom.emailOutreach.service.MailgunServices;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.entity.ppi.FormStatus;
import com.sharkdom.entity.ppi.OrganizationFormRequest;

import com.sharkdom.offlinePartner.entity.OfflinePartnerInvite;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.ppi.OrganizationFormRequestRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationFormRequestService {

    private final OrganizationFormRequestRepository repository;

    private final OrganizationRepository organizationRepository;

    private final OrganizationCollaborationRepository organizationCollaborationRepository;

    private final MailgunServices mailgunServices;

    private final OfflinePartnerInviteRepository offlinePartnerInviteRepository;

    @Transactional
    public OrganizationFormRequest createFormRequest(
            OrganizationFormRequestCreateDto dto
    ) {

        // Receiver org (from token)
        Long receiverOrgId = Util.getOrgIdFromToken();

        // Sender org (from email)
        Organization senderOrg = organizationRepository
                .findByPrimaryEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Organization not found with email: " + dto.getEmail()
                        )
                );

        OrganizationFormRequest request =
                OrganizationFormRequest.builder()
                        .senderOrgId(senderOrg.getId())
                        .receiverOrgId(receiverOrgId)
                        .formId(dto.getFormId())
                        .status(dto.getStatus())
                        .build();

        // Status-based timestamps
        if (dto.getStatus() == FormStatus.APPROVED) {
            request.setAcceptedAt(LocalDateTime.now());
            OrganizationCollaboration organizationCollaboration=new OrganizationCollaboration();
            organizationCollaboration.setSenderOrganizationId(senderOrg.getId());
            organizationCollaboration.setReceiverOrganizationId(receiverOrgId);
            organizationCollaboration.setStatus("ACTIVE");
            organizationCollaboration.setSenderOrganizationName(senderOrg.getName());
            organizationCollaboration.setReceiverOrganizationName(organizationRepository.findNameById(Util.getOrgIdFromToken()));
            var save = organizationCollaborationRepository.save(organizationCollaboration);
        }

        if (dto.getStatus() == FormStatus.DENIED) {
            request.setDeniedAt(LocalDateTime.now());
        }

        return repository.save(request);
    }

    public FormStatusResponseDto getCurrentStatusWithTime(
            String formId,
            String senderEmail
    ) {
        var senderOrg = organizationRepository
                .findByPrimaryEmail(senderEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Organization not found with email: " + senderEmail
                        )
                );
        return repository
                .findByFormIdAndSenderOrgId(formId, senderOrg.getId())
                .map(req -> {

                    if (req.getStatus() == FormStatus.APPROVED) {
                        return new FormStatusResponseDto(
                                FormStatus.APPROVED.name(),
                                req.getAcceptedAt()
                        );
                    }

                    if (req.getStatus() == FormStatus.DENIED) {
                        return new FormStatusResponseDto(
                                FormStatus.DENIED.name(),
                                req.getDeniedAt()
                        );
                    }

                    return new FormStatusResponseDto(
                            "Waiting for Response",
                            null
                    );
                })
                .orElse(
                        new FormStatusResponseDto(
                                "Waiting for Response",
                                null
                        )
                );
    }


    @Transactional
    public OrganizationFormRequest createFormRequestV1(
            OrganizationFormRequestCreateDto dto
    ) {

        Long receiverOrgId = Util.getOrgIdFromToken();

        Organization senderOrg = organizationRepository
                .findByPrimaryEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Organization not found with email: " + dto.getEmail()
                        )
                );

        OrganizationFormRequest request = OrganizationFormRequest.builder()
                .senderOrgId(senderOrg.getId())
                .receiverOrgId(receiverOrgId)
                .formId(dto.getFormId())
                .status(dto.getStatus())
                .build();

        // Handle External vs Internal flow
        if (dto.isExternalUser()) {
            handleExternalUserFlow(dto, senderOrg, receiverOrgId, request);
        } else {
            handleInternalUserFlow(dto, senderOrg, receiverOrgId, request);
        }

        return repository.save(request);
    }

    private void handleInternalUserFlow(
            OrganizationFormRequestCreateDto dto,
            Organization senderOrg,
            Long receiverOrgId,
            OrganizationFormRequest request
    ) {

        if (dto.getStatus() == FormStatus.APPROVED) {

            request.setAcceptedAt(LocalDateTime.now());

            createCollaboration(senderOrg, receiverOrgId);

        }

        if (dto.getStatus() == FormStatus.DENIED) {
            request.setDeniedAt(LocalDateTime.now());
        }
    }


    private void handleExternalUserFlow(
            OrganizationFormRequestCreateDto dto,
            Organization senderOrg,
            Long receiverOrgId,
            OrganizationFormRequest request
    ) {
        if (dto.getStatus() == FormStatus.APPROVED) {
            request.setAcceptedAt(LocalDateTime.now());

            OfflinePartnerInvite invite=new OfflinePartnerInvite();
            invite.setOrganizationId(receiverOrgId);
            invite.setEmail(dto.getEmail());
            invite.setPartnerName(senderOrg.getName());
            invite.setStatus("PENDING");
            offlinePartnerInviteRepository.save(invite);
        }
        if (dto.getStatus() == FormStatus.DENIED) {
            request.setDeniedAt(LocalDateTime.now());
        }
    }

    private void createCollaboration(Organization senderOrg, Long receiverOrgId) {

        OrganizationCollaboration collaboration = new OrganizationCollaboration();

        collaboration.setSenderOrganizationId(senderOrg.getId());
        collaboration.setReceiverOrganizationId(receiverOrgId);
        collaboration.setStatus("ACTIVE");

        collaboration.setSenderOrganizationName(senderOrg.getName());

        collaboration.setReceiverOrganizationName(
                organizationRepository.findNameById(receiverOrgId)
        );

        organizationCollaborationRepository.save(collaboration);
    }


}
