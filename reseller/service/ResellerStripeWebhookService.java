package com.sharkdom.reseller.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.reseller.entity.ResellerStripeEvent;
import com.sharkdom.reseller.repository.ResellerStripeEventRepository;
import com.sharkdom.service.stripe.StripeWebhookQueryService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResellerStripeWebhookService {

    @Value("${stripe.webhook-secret-reseller}")
    private String webhookSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ResellerStripeEventRepository stripeEventRepository;

    private final StripeWebhookQueryService stripeWebhookQueryService;

    @Transactional
    public ResponseEntity<String> handleStripeWebhook(final String payload, final String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            stripeEventRepository.save(ResellerStripeEvent.builder()
                    .eventId(event.getId())
                    .eventType(event.getType())
                    .idempotencyKey(event.getRequest().getIdempotencyKey())
                    .eventObjectDetails(event.getDataObjectDeserializer().getObject()
                            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH56))
                            .toString())
                    .build());
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook signature verification failed");
        } catch (Exception e) {
            log.error("Invalid payload : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid payload");
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            log.info("Payload detail : {}", jsonNode.toString());
            evenTypeDatabaseUpdate(event);
            log.info("Webhook received");
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            log.error("Error processing webhook event : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing webhook event");
        }
    }


    private void evenTypeDatabaseUpdate(Event event) throws Exception {
        switch (event.getType()) {
            case "checkout.session.completed":
                log.info("Started Event : checkout.session.completed");
                stripeWebhookQueryService.handleCompletedCheckoutSession(event);
                log.info("Finished Event: checkout.session.completed. Checkout Session Database updated successfully");
                break;

            case "checkout.session.expired":
                log.info("Started Event : checkout.session.expired");
                stripeWebhookQueryService.handleCompletedCheckoutExpired(event);
                break;

            case "invoice.payment_succeeded":
                log.info("Started Event : invoice.payment_succeeded");
                log.info("Finished Event: invoice.payment_succeeded");
                break;

            case "payment_intent.succeeded":
                log.info("Event : payment_intent.succeeded.");
                stripeWebhookQueryService.handlePaymentIntentSucceeded(event);
                log.info("Finished Event: payment_intent.succeeded.");
                break;

            case "charge.succeeded":
                log.info("Event : charge.succeeded.");
                stripeWebhookQueryService.handleChargeSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                log.info("Event : payment_intent.payment_failed.");
                stripeWebhookQueryService.handlePaymentIntentFailed(event);
                break;

            case "invoice.paid":
                log.info("Event : invoice.paid.");
                stripeWebhookQueryService.handleInvoicePaidEvent(event);
                break;

            case "invoice.payment_failed":
                log.info("Event : invoice.payment_failed.");
                stripeWebhookQueryService.handleInvoicePaymentFailedEvent(event);
                break;

            case "charge.updated":
                log.info("Event : charge.updated.");
                Charge charge = (Charge) event.getDataObjectDeserializer().getObject()
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH57));
                Charge retrievedCharge = Charge.retrieve(charge.getId());
                log.info("Charge : {}", retrievedCharge.getId());
                break;

            case "customer.balance_transaction.created":
                log.info("Event : customer.balance_transaction.created.");
                stripeWebhookQueryService.handleCustomerBalanceTransactionCreated(event);
                break;

            case "customer.subscription.updated":
                log.info("Event : customer.subscription.updated.");
                stripeWebhookQueryService.handleSubscriptionUpdated(event);
                break;

            case "customer.subscription.created":
                log.info("Event : customer.subscription.created.");
                stripeWebhookQueryService.handleSubscriptionCreated(event);
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

}
