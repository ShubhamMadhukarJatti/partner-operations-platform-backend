package com.sharkdom.controller.stripe;

import com.sharkdom.entity.stripe.StripeEvent;
import com.sharkdom.service.stripe.StripeWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/sharkdom-stripe/v1")
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @Hidden
    @PermitAll
    @PostMapping("/webhook/subscription")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        return stripeWebhookService.handleStripeWebhook(payload, sigHeader);
    }

    @PermitAll
    @GetMapping("/eventById/{id}")
    public ResponseEntity<StripeEvent> getStripeWebhookEventDetail(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(stripeWebhookService.getStripeWebhookEventDetail(id));
    }

    @PermitAll
    @GetMapping("/eventByEventId/{eventId}")
    public ResponseEntity<List<StripeEvent>> getStripeWebhookEventDetailByEventId(@PathVariable(value = "eventId") String eventId) {
        return ResponseEntity.ok(stripeWebhookService.getStripeWebhookEventDetailByEventId(eventId));
    }
}
