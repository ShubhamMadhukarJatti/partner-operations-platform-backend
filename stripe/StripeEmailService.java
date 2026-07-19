package com.sharkdom.service.stripe;

import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.stripe.StripeSubscriptionStatus;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.stripe.StripeCheckoutSessions;
import com.sharkdom.entity.stripe.StripeCustomer;
import com.sharkdom.entity.stripe.StripeSubscriptionData;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.notification.NotificationService;
import com.stripe.model.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeEmailService {

    private final EmailService emailService;

    private final WebSocketHandler webSocketHandler;

    private final NotificationService notificationService;

    private static final String NOTIFICATION_SUBJECT_MESSAGE = "Subscription ";

    private static final String NOTIFICATION_BODY_MESSAGE = "Congrats! You have successfully ";

    private final OrganizationRepository organizationRepository;

    private final StripeService stripeService;

    protected Organization getOrganizationByOrganizationId(Long organizationId, ErrorMessages errorMessage) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage, organizationId));
    }

    void sendSubscriptionUpgradeDowngradeSuccessEmailAndNotification(StripeSubscriptionData savedStripeSubscriptionData) throws Exception {
        String templateCode = "Upgrade-downgrade-success";
        log.info("Event: checkout.session.completed. Template Code : {}", templateCode);
        for (Long organizationId : savedStripeSubscriptionData.getOrganizationId()) {
            Organization organizationByOrganizationId = getOrganizationByOrganizationId(organizationId, ErrorMessages.SH49);
            Invoice stripeInvoice = stripeService.getStripeInvoice(savedStripeSubscriptionData.getLatestInvoice());
            InputStream downloadPdfAsInputStream = stripeService.downloadPdfAsInputStream(stripeInvoice.getInvoicePdf());
            emailService.sendEmailForUpgradeStripeSubscription(templateCode, organizationByOrganizationId.getName(), savedStripeSubscriptionData, downloadPdfAsInputStream);

            Notification notification = Notification.builder()
                    .subject("Subscription changed")
                    .body("Congrats! You have successfully changed a subscription.")
                    .forWeb(true)
                    .organizationId(organizationByOrganizationId.getId())
                    .build();

            webSocketHandler.sendMessageToUser(organizationByOrganizationId.getId(), notification);
            notificationService.create(notification);
        }
    }

    void sendPaymentSuccessEmailAndNotificationAfterSubscriptionPurchased(StripeSubscriptionData subscription) throws Exception {
        String templateCode = "subscription_free_trial";
        if (subscription.getStatus().equals(StripeSubscriptionStatus.TRIALING)) {
            templateCode = "subscription_free_trial";
        } else if (subscription.getStatus().equals(StripeSubscriptionStatus.ACTIVE)) {
            templateCode = "payment_subscription";
        }
        log.info("Event: checkout.session.completed. Template Code : {}", templateCode);
        for (Long organizationId : subscription.getOrganizationId()) {
            Organization organizationByOrganizationId = getOrganizationByOrganizationId(organizationId, ErrorMessages.SH49);
            Invoice stripeInvoice = stripeService.getStripeInvoice(subscription.getLatestInvoice());
            InputStream downloadPdfAsInputStream = stripeService.downloadPdfAsInputStream(stripeInvoice.getInvoicePdf());
            emailService.sendEmailForStripeSubscriptionMode(templateCode, organizationByOrganizationId.getName(), subscription, downloadPdfAsInputStream);

            Notification notification = Notification.builder()
                    .subject("Subscription bought")
                    .body("Congrats! You have successfully bought a subscription.")
                    .forWeb(true)
                    .organizationId(organizationByOrganizationId.getId())
                    .build();

            webSocketHandler.sendMessageToUser(organizationByOrganizationId.getId(), notification);
            notificationService.create(notification);
        }

    }

    void sendPaymentSuccessEmailAndNotificationForPayment(StripeCheckoutSessions completedCheckoutSessions) throws Exception {
        String templateCode = "payment_onetime";
        log.info("Event: checkout.session.completed. Template Code: {}", templateCode);
        for (Long organizationId : completedCheckoutSessions.getCustomer().getOrganizationId()) {
            Organization organization = getOrganizationByOrganizationId(organizationId, ErrorMessages.SH81);
            emailService.sendEmailForStripePaymentMode(templateCode, organization.getName(), completedCheckoutSessions);

            Notification notification = Notification.builder()
                    .subject("Plan bought")
                    .body("Congrats! You have successfully bought a Plan.")
                    .forWeb(true)
                    .organizationId(organization.getId())
                    .build();

            webSocketHandler.sendMessageToUser(organization.getId(), notification);
            notificationService.create(notification);
        }
    }

    void sendPaymentFailureEmailAndNotificationForInvoicePaymentFailed(Set<Long> organizationIds, StripeSubscriptionData subscription) throws Exception {
        String templateCode = "invoice_payment_failure";
        log.info("Event : invoice.paid. Template Code : {}", templateCode);
        for (Long organizationId : organizationIds) {
            Organization organization = organizationRepository.findById(organizationId).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH50));
//            emailService.sendEmailForStripeSubscriptionMode(templateCode, organization.getName(), subscription);

            Notification notification = Notification.builder()
                    .subject("Subscription upgradation payment failure")
                    .body("You payment failed for new subscription.")
                    .forWeb(true)
                    .organizationId(organization.getId())
                    .build();

            webSocketHandler.sendMessageToUser(organization.getId(), notification);
            notificationService.create(notification);
        }
    }

    void sendConfirmationEmailAndNotification(StripeSubscriptionData savedSubscriptionData, String upgradeMessage, StripeSubscriptionData currentSubscription) throws Exception {
        for (Long organizationId : savedSubscriptionData.getOrganizationId()) {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH51, upgradeMessage));
            Invoice stripeInvoice = stripeService.getStripeInvoice(currentSubscription.getLatestInvoice());
            InputStream downloadPdfAsInputStream = stripeService.downloadPdfAsInputStream(stripeInvoice.getInvoicePdf());
            emailService.sendEmailForStripeSubscriptionMode("payment_subscription", organization.getName(), savedSubscriptionData, downloadPdfAsInputStream);

            Notification notification = Notification.builder()
                    .subject(NOTIFICATION_SUBJECT_MESSAGE + upgradeMessage)
                    .body(NOTIFICATION_BODY_MESSAGE + upgradeMessage + " a subscription from " + currentSubscription.getPrice().getPlanType().getPlanName() + " to " + savedSubscriptionData.getPrice().getPlanType().getPlanName())
                    .forWeb(true)
                    .organizationId(organization.getId())
                    .build();

            webSocketHandler.sendMessageToUser(organization.getId(), notification);
            notificationService.create(notification);
        }
    }


    void sendEmailForSubscriptionAutoRenewalFailed(StripeCustomer customer, Invoice invoice, StripeSubscriptionData retrievedStripeSubscription) throws Exception {
        for (Long organizationId : customer.getOrganizationId()) {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH52));
            emailService.sendEmailForSubscriptionAutoRenewalFailed("payment_link", organization.getName(), invoice);

            Notification notification = Notification.builder()
                    .subject("Payment failed for renewal")
                    .body("Payment failed for a subscription renewal of " + retrievedStripeSubscription.getPrice().getPlanType().getPlanName() + ".Payment link: " + invoice.getHostedInvoiceUrl())
                    .forWeb(true)
                    .organizationId(organization.getId())
                    .build();

            webSocketHandler.sendMessageToUser(organization.getId(), notification);
            notificationService.create(notification);
        }
    }


}
