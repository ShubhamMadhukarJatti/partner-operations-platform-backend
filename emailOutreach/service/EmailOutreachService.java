package com.sharkdom.emailOutreach.service;

import com.sharkdom.emailOutreach.dto.MessageEventSummary;
import com.sharkdom.emailOutreach.dto.OrgMessageEventSummary;
import com.sharkdom.emailOutreach.dto.SendMailRequest;
import com.sharkdom.emailOutreach.dto.SendMailResponse;
import com.sharkdom.emailOutreach.entity.Email;
import com.sharkdom.emailOutreach.entity.EmailAccount;
import com.sharkdom.emailOutreach.entity.MessageEvent;
import com.sharkdom.emailOutreach.repository.EmailAccountRepository;
import com.sharkdom.emailOutreach.repository.EmailRepository;
import com.sharkdom.emailOutreach.repository.MessageEventRepository;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.offlinePartner.entity.OfflinePartnerInvite;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmailOutreachService {

    @Autowired
    private EmailAccountRepository emailAccountRepository;

    @Autowired
    private MailgunServices mailgunServices;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private MessageEventRepository messageEventRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OfflinePartnerInviteRepository offlinePartnerInviteRepository;

    public EmailAccount claimOrCreateEmail() {
        Long orgId = Util.getOrgIdFromToken();

        // Check if email account already exists
        Optional<EmailAccount> existingAccountOpt = emailAccountRepository.findByOrganizationId(orgId);

        EmailAccount emailAccount;
        if (existingAccountOpt.isPresent()) {
            // Update existing account
            emailAccount = existingAccountOpt.get();
            emailAccount.setClaimed(true);
        } else {
            // Create new account
            emailAccount = new EmailAccount();
            emailAccount.setOrganizationId(orgId);
            emailAccount.setClaimed(true);
        }

        return emailAccountRepository.save(emailAccount);
    }


    public EmailAccount getEmailAccount() {
        return emailAccountRepository
                .findByOrganizationId(Util.getOrgIdFromToken())
                .orElse(null);
    }

    public SendMailResponse sendEmail(SendMailRequest sendMailRequest) {
        log.info("Preparing to send email to: {}", sendMailRequest.getTo());
        String primaryEmail=null;
        String senderPrimaryEmail=null;
        try {
            log.info("Sending email via Mailgun...");
            Optional<Organization> organization = organizationRepository.findById(sendMailRequest.getPartnerId());
            if (organization.isPresent()) {
                primaryEmail = organization.get().getPrimaryEmail();
                sendMailRequest.setTo(primaryEmail);
                sendMailRequest.setFrom(sendMailRequest.getFrom());
            }
            SendMailResponse sendMailResponse = mailgunServices.sendEmail(sendMailRequest);

            if (sendMailResponse == null || sendMailResponse.getId() == null) {
                log.error("Failed to send email. Mailgun response is null or invalid.");
                SendMailResponse errorResponse = new SendMailResponse();
                errorResponse.setId("error");
                errorResponse.setMessage("Failed to send email. Invalid Mailgun response.");
                return errorResponse;
            }

            // Save email record only if valid
            Email email = new Email();
            email.setTo(primaryEmail);
            email.setSubject(sendMailRequest.getSubject());
            email.setBody(sendMailRequest.getBody());
            email.setFrom(sendMailRequest.getFrom());
            email.setMessageId(sendMailResponse.getId());
            email.setThreadId(sendMailResponse.getThreadId());
            if (sendMailRequest.getPartnerId()>0) {
                email.setPartnerOrgId(sendMailRequest.getPartnerId());
            }
            email.setOrgId(Util.getOrgIdFromToken());
            Optional<Organization> senderOrg = organizationRepository.findById(Util.getOrgIdFromToken());
            if (senderOrg.isPresent()) {
                email.setSenderOrgEmail(senderOrg.get().getPrimaryEmail());
            }
            Email saved = emailRepository.save(email);
            log.info("Email record saved with ID: {}", saved.getId());

            log.info("Email sent successfully. Mailgun message: {}", sendMailResponse.getMessage());
            return sendMailResponse;

        } catch (Exception e) {
            log.error("Error sending email to: {}", sendMailRequest.getTo(), e);
            SendMailResponse errorResponse = new SendMailResponse();
            errorResponse.setId("error");
            errorResponse.setMessage("Exception occurred: " + e.getMessage());
            return errorResponse;
        }
    }

    public SendMailResponse sendEmailExternalPartner(SendMailRequest sendMailRequest) {
        log.info("Preparing to send email to: {}", sendMailRequest.getTo());
        SendMailResponse sendMailResponse =null;
        try {
            log.info("Sending email via Mailgun...");
            Optional<OfflinePartnerInvite> partnerMessageCode = offlinePartnerInviteRepository.findByOfflinePartnerMessageCode(sendMailRequest.getExternalPartnerCode());
            if (partnerMessageCode.isPresent()) {
                sendMailRequest.setTo(partnerMessageCode.get().getEmail());
                sendMailResponse = mailgunServices.sendEmail(sendMailRequest);
            }
            if (sendMailResponse == null || sendMailResponse.getId() == null) {
                log.error("Failed to send email. Mailgun response is null or invalid.");

                SendMailResponse errorResponse = new SendMailResponse();
                errorResponse.setId("error");
                errorResponse.setMessage("Failed to send email. Invalid Mailgun response.");
                return errorResponse;
            }

            // Save email record only if valid
            Email email = new Email();
            email.setTo(sendMailRequest.getTo());
            email.setSubject(sendMailRequest.getSubject());
            email.setBody(sendMailRequest.getBody());
            email.setFrom(sendMailRequest.getFrom());
            email.setMessageId(sendMailResponse.getId());
            email.setThreadId(sendMailResponse.getThreadId());
            email.setExternalPartner(true);
            email.setExternalPartnerCode(sendMailRequest.getExternalPartnerCode());
            email.setOrgId(Util.getOrgIdFromToken());
            Email saved = emailRepository.save(email);
            log.info("Email record saved with ID: {}", saved.getId());

            log.info("Email sent successfully. Mailgun message: {}", sendMailResponse.getMessage());
            return sendMailResponse;

        } catch (Exception e) {
            log.error("Error sending email to: {}", sendMailRequest.getTo(), e);
            SendMailResponse errorResponse = new SendMailResponse();
            errorResponse.setId("error");
            errorResponse.setMessage("Exception occurred: " + e.getMessage());
            return errorResponse;
        }
    }

    public OrgMessageEventSummary getOrgMessageEventSummary() {
        Long orgId = Util.getOrgIdFromToken();
        List<Email> emails = emailRepository.findByOrgId(orgId);

        OrgMessageEventSummary summary = new OrgMessageEventSummary();
        summary.setOrgId(orgId);
        summary.setTotalEmails((long) emails.size());

        long opened = 0, delivered = 0, bounced = 0, dropped = 0, complained = 0, unsubscribed = 0, clicked=0, accepted=0;

        for (Email email : emails) {
            if (email.getMessageId() == null || email.getMessageId().isEmpty()) {
                continue;
            }

            // remove < and > from messageId
            String cleanMessageId = email.getMessageId().replaceAll("[<>]", "");
            log.info("Querying events with messageId={}", cleanMessageId);

            List<MessageEvent> events = messageEventRepository.findByMessageId(cleanMessageId);

            for (MessageEvent event : events) {
                switch (event.getEvent().toLowerCase()) {
                    case "opened" -> opened++;
                    case "delivered" -> delivered++;
                    case "bounced" -> bounced++;
                    case "dropped" -> dropped++;
                    case "complained" -> complained++;
                    case "unsubscribed" -> unsubscribed++;
                    case "clicked" -> clicked++;
                    case "accepted" -> accepted++;
                    default -> log.debug("Unhandled event: {}", event.getEvent());
                }
            }
        }
        summary.setOpened(opened);
        summary.setDelivered(delivered);
        summary.setBounced(bounced);
        summary.setDropped(dropped);
        summary.setComplained(complained);
        summary.setUnsubscribed(unsubscribed);
        summary.setAccepted(accepted);
        summary.setClicked(clicked);
        if (delivered > 0) {
            double openRate = Math.round(((double) opened / delivered) * 1000.0) / 10.0;
            double clickRate = Math.round(((double) clicked / delivered) * 1000.0) / 10.0;
            double engagementRate = Math.round(((double) (opened + clicked) / delivered) * 1000.0) / 10.0;

            summary.setEngagementRate(engagementRate);
            summary.setOpenRate(openRate);
            summary.setClickRate(clickRate);
        } else {
            summary.setOpenRate(0.0);
            summary.setClickRate(0.0);
            summary.setEngagementRate(0.0);
        }
        return summary;
    }

    public MessageEventSummary getMessageEventSummaryById(String messageId) {
        MessageEventSummary summary = new MessageEventSummary();
        long opened = 0, delivered = 0, bounced = 0, dropped = 0, complained = 0, unsubscribed = 0, clicked = 0, accepted = 0;
        log.info("Querying events with messageId={}", messageId);
        List<MessageEvent> events = messageEventRepository.findByMessageId(messageId);
        for (MessageEvent event : events) {
            switch (event.getEvent().toLowerCase()) {
                case "opened" -> opened++;
                case "delivered" -> delivered++;
                case "bounced" -> bounced++;
                case "dropped" -> dropped++;
                case "complained" -> complained++;
                case "unsubscribed" -> unsubscribed++;
                case "clicked" -> clicked++;
                case "accepted" -> accepted++;
                default -> log.debug("Unhandled event: {}", event.getEvent());
            }
        }
        summary.setOpened(opened);
        summary.setDelivered(delivered);
        summary.setBounced(bounced);
        summary.setDropped(dropped);
        summary.setComplained(complained);
        summary.setUnsubscribed(unsubscribed);
        summary.setAccepted(accepted);
        summary.setClicked(clicked);
        return summary;
    }


    public OrgMessageEventSummary getOrgMessageEventSummaryByEmails(List<Long> partnerIds) {
        log.info("Getting message event summary for partner IDs: {}", partnerIds);
        OrgMessageEventSummary summary = new OrgMessageEventSummary();
        List<Organization> organizations = organizationRepository.findByIdIn(partnerIds);
        for (Organization organization:organizations) {
            Long orgId = Util.getOrgIdFromToken();
            List<Email> emails = emailRepository.findByOrgIdAndTo(orgId, organization.getPrimaryEmail());
            summary.setOrgId(orgId);
            summary.setTotalEmails((long) emails.size());
            long opened = 0, delivered = 0, bounced = 0, dropped = 0, complained = 0, unsubscribed = 0, clicked = 0, accepted = 0;
            for (Email email : emails) {
                if (email.getMessageId() == null || email.getMessageId().isEmpty()) {
                    continue;
                }

                // remove < and > from messageId
                String cleanMessageId = email.getMessageId().replaceAll("[<>]", "");
                log.info("Querying events with messageId={}", cleanMessageId);

                List<MessageEvent> events = messageEventRepository.findByMessageId(cleanMessageId);

                for (MessageEvent event : events) {
                    switch (event.getEvent().toLowerCase()) {
                        case "opened" -> opened++;
                        case "delivered" -> delivered++;
                        case "bounced" -> bounced++;
                        case "dropped" -> dropped++;
                        case "complained" -> complained++;
                        case "unsubscribed" -> unsubscribed++;
                        case "clicked" -> clicked++;
                        case "accepted" -> accepted++;
                        default -> log.debug("Unhandled event: {}", event.getEvent());
                    }
                }
            }
            summary.setOpened(opened);
            summary.setDelivered(delivered);
            summary.setBounced(bounced);
            summary.setDropped(dropped);
            summary.setComplained(complained);
            summary.setUnsubscribed(unsubscribed);
            summary.setAccepted(accepted);
            summary.setClicked(clicked);
            if (delivered > 0) {
                double openRate = Math.round(((double) opened / delivered) * 1000.0) / 10.0;
                double clickRate = Math.round(((double) clicked / delivered) * 1000.0) / 10.0;
                double engagementRate = Math.round(((double) (opened + clicked) / delivered) * 1000.0) / 10.0;

                summary.setEngagementRate(engagementRate);
                summary.setOpenRate(openRate);
                summary.setClickRate(clickRate);
            } else {
                summary.setOpenRate(0.0);
                summary.setClickRate(0.0);
                summary.setEngagementRate(0.0);
            }
        }
        return summary;
    }

    public OrgMessageEventSummary getOrgMessageEventSummaryByEmailsForExternalPartner(String externalPartnerCode) {
        log.info("Getting message event summary for partner IDs: {}", externalPartnerCode);
        OrgMessageEventSummary summary = new OrgMessageEventSummary();
            Long orgId = Util.getOrgIdFromToken();
            List<Email> emails = emailRepository.findByIsExternalPartnerTrueAndOrgIdAndExternalPartnerCode(orgId,externalPartnerCode);
            summary.setOrgId(orgId);
            summary.setTotalEmails((long) emails.size());
            long opened = 0, delivered = 0, bounced = 0, dropped = 0, complained = 0, unsubscribed = 0, clicked = 0, accepted = 0;
            for (Email email : emails) {
                if (email.getMessageId() == null || email.getMessageId().isEmpty()) {
                    continue;
                }

                // remove < and > from messageId
                String cleanMessageId = email.getMessageId().replaceAll("[<>]", "");
                log.info("Querying events with messageId={}", cleanMessageId);

                List<MessageEvent> events = messageEventRepository.findByMessageId(cleanMessageId);

                for (MessageEvent event : events) {
                    switch (event.getEvent().toLowerCase()) {
                        case "opened" -> opened++;
                        case "delivered" -> delivered++;
                        case "bounced" -> bounced++;
                        case "dropped" -> dropped++;
                        case "complained" -> complained++;
                        case "unsubscribed" -> unsubscribed++;
                        case "clicked" -> clicked++;
                        case "accepted" -> accepted++;
                        default -> log.debug("Unhandled event: {}", event.getEvent());
                    }
                }
            }
            summary.setOpened(opened);
            summary.setDelivered(delivered);
            summary.setBounced(bounced);
            summary.setDropped(dropped);
            summary.setComplained(complained);
            summary.setUnsubscribed(unsubscribed);
            summary.setAccepted(accepted);
            summary.setClicked(clicked);

        double openRate = Math.round(((double) opened / delivered) * 1000.0) / 10.0;
        double clickRate = Math.round(((double) clicked / delivered) * 1000.0) / 10.0;
        double engagementRate = Math.round(((double) (opened + clicked) / delivered) * 1000.0) / 10.0;

        summary.setEngagementRate(engagementRate);
        summary.setOpenRate(openRate);
        summary.setClickRate(clickRate);

        return summary;
    }

    public List<Email> getSendEmails() {
        Long orgId = Util.getOrgIdFromToken();
        return emailRepository.findByOrgId(orgId);
    }

    public List<Email> getReceivedEmails() {
        Long orgId = Util.getOrgIdFromToken();
        return emailRepository.findByPartnerOrgId(orgId);
    }

    public OrgMessageEventSummary getOrgMessageEventSummaryBasedOnOrgId() {
        log.info("Getting message event summary based on org ID");
        OrgMessageEventSummary summary = new OrgMessageEventSummary();
            Long orgId = Util.getOrgIdFromToken();
            List<Email> emails = emailRepository.findByOrgId(orgId);
            summary.setOrgId(orgId);
            summary.setTotalEmails((long) emails.size());
            long opened = 0, delivered = 0, bounced = 0, dropped = 0, complained = 0, unsubscribed = 0, clicked = 0, accepted = 0;
            for (Email email : emails) {
                if (email.getMessageId() == null || email.getMessageId().isEmpty()) {
                    continue;
                }
                // remove < and > from messageId
                String cleanMessageId = email.getMessageId().replaceAll("[<>]", "");
                log.info("Querying events with messageId={}", cleanMessageId);

                List<MessageEvent> events = messageEventRepository.findByMessageId(cleanMessageId);

                for (MessageEvent event : events) {
                    switch (event.getEvent().toLowerCase()) {
                        case "opened" -> opened++;
                        case "delivered" -> delivered++;
                        case "bounced" -> bounced++;
                        case "dropped" -> dropped++;
                        case "complained" -> complained++;
                        case "unsubscribed" -> unsubscribed++;
                        case "clicked" -> clicked++;
                        case "accepted" -> accepted++;
                        default -> log.debug("Unhandled event: {}", event.getEvent());
                    }
                }
            }
            summary.setOpened(opened);
            summary.setDelivered(delivered);
            summary.setBounced(bounced);
            summary.setDropped(dropped);
            summary.setComplained(complained);
            summary.setUnsubscribed(unsubscribed);
            summary.setAccepted(accepted);
            summary.setClicked(clicked);
            if (delivered > 0) {
                double openRate = Math.round(((double) opened / delivered) * 1000.0) / 10.0;
                double clickRate = Math.round(((double) clicked / delivered) * 1000.0) / 10.0;
                double engagementRate = Math.round(((double) (opened + clicked) / delivered) * 1000.0) / 10.0;

                summary.setEngagementRate(engagementRate);
                summary.setOpenRate(openRate);
                summary.setClickRate(clickRate);
            } else {
                summary.setOpenRate(0.0);
                summary.setClickRate(0.0);
                summary.setEngagementRate(0.0);
            }
            return summary;
    }

}
