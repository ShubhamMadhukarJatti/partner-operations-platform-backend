package com.sharkdom.controller.stripe;

import com.sharkdom.model.stripe.PaymentIntentResponse;
import com.sharkdom.model.stripe.PayoutBankAccountRequest;
import com.sharkdom.model.stripe.StripeAccountResponse;
import com.sharkdom.model.stripe.StripePayoutAccountResponse;
import com.sharkdom.service.stripe.StripePayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stripe/payout")
public class StripePayoutController {

    @Autowired
    StripePayoutService stripeService;

    @PostMapping("/add-bank")
    public ResponseEntity<StripePayoutAccountResponse> addBankAccount(@RequestBody PayoutBankAccountRequest request) {
        StripePayoutAccountResponse response = stripeService.addExternalBankAccount(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/stripe-onbording")
    public ResponseEntity<Map<String, String>> createAccountLink(
            @RequestParam String accountId,@RequestParam String returnUrl,@RequestParam String refreshUrl ) {
        String onboardingUrl = stripeService.createAccountOnboardingLink(
                accountId,returnUrl,refreshUrl);

        Map<String, String> response = new HashMap<>();
        response.put("onboardingUrl", onboardingUrl);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/create-payment")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestParam int amount,
            @RequestParam String currency) {

        PaymentIntentResponse response = stripeService.createPaymentIntent(amount, currency);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/create-account")
    public ResponseEntity<StripeAccountResponse> createAccount(
            @RequestParam String email,
            @RequestParam String country
    ) {
        StripeAccountResponse response = stripeService.createAccount(email, country);
        return ResponseEntity.ok(response);
    }
}
