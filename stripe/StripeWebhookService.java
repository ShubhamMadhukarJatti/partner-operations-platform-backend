package com.sharkdom.service.stripe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.stripe.StripeEvent;

import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.repository.stripe.StripeEventRepository;

import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.subscription.entity.OrganizationSubscription;
import com.sharkdom.subscription.repository.OrganizationSubscriptionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookService {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final StripeEventRepository stripeEventRepository;
    private final StripeWebhookQueryService stripeWebhookQueryService;
    private final OrganizationSubscriptionRepository orgSubRepo;

    // ===================== MAIN WEBHOOK =====================

    @Transactional
    public ResponseEntity<String> handleStripeWebhook(final String payload, final String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            // IDEMPOTENCY CHECK (IMPORTANT)
            boolean alreadyProcessed =
                    stripeEventRepository.existsByEventId(event.getId());

            if (alreadyProcessed) {
                log.warn("[WEBHOOK DUPLICATE] eventId={} already processed", event.getId());
                return ResponseEntity.ok("Already processed");
            }

            stripeEventRepository.save(StripeEvent.builder()
                    .eventId(event.getId())
                    .eventType(event.getType())
                    .idempotencyKey(event.getRequest() != null
                            ? event.getRequest().getIdempotencyKey()
                            : null)
                    .eventObjectDetails(event.getDataObjectDeserializer().getObject()
                            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH56))
                            .toString())
                    .build());

        } catch (SignatureVerificationException e) {
            log.error("[WEBHOOK ERROR] Signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("[WEBHOOK ERROR] Invalid payload: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            log.info("[WEBHOOK RECEIVED] type={} payload={}", event.getType(), jsonNode);

            processEvent(event);

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            log.error("[WEBHOOK PROCESS ERROR] eventId={} error={}",
                    event.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Processing failed");
        }
    }

    // ===================== EVENT ROUTER =====================

    private void processEvent(Event event) throws Exception {

        switch (event.getType()) {

            case "invoice.paid":
                handlePaymentSuccess(event);
                stripeWebhookQueryService.handleInvoicePaidEvent(event);
                break;

            case "invoice.payment_failed":
                handlePaymentFailed(event);
                stripeWebhookQueryService.handleInvoicePaymentFailedEvent(event);
                break;

            case "customer.subscription.deleted":
                handleCancel(event);
                break;

            case "customer.subscription.updated":
                stripeWebhookQueryService.handleSubscriptionUpdated(event);
                break;

            case "customer.subscription.created":
                stripeWebhookQueryService.handleSubscriptionCreated(event);
                break;

            default:
                log.info("[WEBHOOK IGNORED] type={}", event.getType());
        }
    }

    // ===================== PAYMENT SUCCESS =====================

    private void handlePaymentSuccess(Event event) {

        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH56));

        OrganizationSubscription sub = orgSubRepo
                .findByStripeSubscriptionId(invoice.getSubscription())
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessages.SH55, invoice.getSubscription()));

        log.info("[PAYMENT SUCCESS] orgId={} subscriptionId={}",
                sub.getOrganizationId(), sub.getStripeSubscriptionId());

        sub.setPaymentStatus(PaymentStatus.ACTIVE);
        sub.setGracePeriodEnd(null);

        orgSubRepo.save(sub);
    }

    // ===================== PAYMENT FAILED =====================

    private void handlePaymentFailed(Event event) {

        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH56));

        OrganizationSubscription sub = orgSubRepo
                .findByStripeSubscriptionId(invoice.getSubscription())
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessages.SH55, invoice.getSubscription()));

        log.warn("[PAYMENT FAILED] orgId={} subscriptionId={}",
                sub.getOrganizationId(), sub.getStripeSubscriptionId());

        // Only set grace if not already in grace
        if (sub.getPaymentStatus() != PaymentStatus.GRACE_PERIOD) {

            sub.setPaymentStatus(PaymentStatus.GRACE_PERIOD);
            sub.setGracePeriodEnd(LocalDateTime.now().plusDays(7));

            log.info("[GRACE STARTED] orgId={} | graceEnd={}",
                    sub.getOrganizationId(), sub.getGracePeriodEnd());

            orgSubRepo.save(sub);
        }
    }

    // ===================== SUBSCRIPTION CANCEL =====================

    private void handleCancel(Event event) {

        Subscription stripeSub = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH56));

        OrganizationSubscription sub = orgSubRepo
                .findByStripeSubscriptionId(stripeSub.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessages.SH55, stripeSub.getId()));

        log.warn("[SUBSCRIPTION CANCELLED] orgId={} subscriptionId={}",
                sub.getOrganizationId(), stripeSub.getId());

        sub.setPaymentStatus(PaymentStatus.FREE);
        sub.setActiveSuites(List.of()); // remove all access

        orgSubRepo.save(sub);
    }

    // ===================== FETCH APIs =====================

    @Transactional
    public StripeEvent getStripeWebhookEventDetail(Long id) {
        return stripeEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH58, id));
    }

    @Transactional
    public List<StripeEvent> getStripeWebhookEventDetailByEventId(String eventId) {
        return stripeEventRepository.findAllByEventId(eventId);
    }
}