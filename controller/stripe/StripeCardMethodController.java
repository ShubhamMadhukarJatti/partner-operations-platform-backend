package com.sharkdom.controller.stripe;

import com.sharkdom.dto.CreateSetupIntentRequest;
import com.sharkdom.dto.SetupIntentResponse;
import com.sharkdom.model.stripe.StripeCardDetailDto;
import com.sharkdom.service.stripe.StripeCardMethodService;
import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sharkdom-stripe/v1")
@RequiredArgsConstructor
public class StripeCardMethodController {

    private final StripeCardMethodService stripeCardMethodService;

    @PostMapping("/card-detail")
    public ResponseEntity<StripeCardDetailDto> getAndSaveCustomerCardDetails(@RequestParam(value = "customerId") String customerId) throws StripeException {
        return ResponseEntity.status(HttpStatus.CREATED).body(stripeCardMethodService.getAndSaveCustomerCardDetails(customerId));
    }

    @PutMapping("/update-card-detail")
    public ResponseEntity<StripeCardDetailDto> updatePaymentMethod(@RequestParam(value = "customerId") String customerId, @RequestParam(value = "paymentMethodId") String paymentMethodId) throws StripeException {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripeCardMethodService.updatePaymentMethod(customerId, paymentMethodId));
    }

    @PostMapping("/create-setup-intent")
    public ResponseEntity<SetupIntentResponse> createSetupIntent(
            @RequestBody CreateSetupIntentRequest request
    ) throws StripeException {
        SetupIntentResponse response =
                stripeCardMethodService.createSetupIntent(request.getCustomerId());
        return ResponseEntity.ok(response);
    }
}
