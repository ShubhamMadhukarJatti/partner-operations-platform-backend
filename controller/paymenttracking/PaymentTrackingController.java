package com.sharkdom.controller.paymenttracking;

import com.sharkdom.model.paymenttracking.RazorpayPaymentDetailsDto;
import com.sharkdom.model.referral.ReferralLinkResponse;
import com.sharkdom.service.paymenttracking.PaymentTrackingService;
import com.sharkdom.service.referral.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping
@Tag(name = "Payment Leads")
public class PaymentTrackingController {
    private final PaymentTrackingService paymentTrackingService;
    private final ReferralService referralService;

    public PaymentTrackingController(PaymentTrackingService paymentTrackingService, ReferralService referralService) {
        this.paymentTrackingService = paymentTrackingService;
        this.referralService = referralService;
    }

    @PostMapping("/tracking/razorpay/{affiliateCode}")
    public ResponseEntity<Object> trackRazorpayPayment(@PathVariable String affiliateCode, @RequestBody Map<Object, Object> payment) {
        paymentTrackingService.trackRazorpayPayment(affiliateCode, payment);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tracking/razorpay/test/{affiliateCode}")
    public ResponseEntity<Object> trackRazorpayPaymentTest(@PathVariable String affiliateCode, @RequestBody Map<Object, Object> payment) {
        paymentTrackingService.trackRazorpayPaymentTest(affiliateCode, payment);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get payment tracking data for test")
    @GetMapping("/tracking/razorpay/data/test")
    public List<RazorpayPaymentDetailsDto> getRazorpayDataTest(@RequestParam String affiliateCode) {
        return referralService.getPaymentTrackingDataTest(affiliateCode);
    }

    @Operation(summary = "Get payment tracking data for test")
    @GetMapping("/tracking/razorpay/data")
    public List<RazorpayPaymentDetailsDto> getRazorpayData(@RequestParam String affiliateCode) {
        return referralService.getPaymentTrackingData(affiliateCode);
    }

    @Operation(summary = "Generate referral link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Referral Link Generated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ReferralLinkResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/referral/generate")
    public ReferralLinkResponse generateReferralLink(@RequestParam Long organizationId, @RequestParam String landingPage) {
        return referralService.generateReferralLink(organizationId, landingPage);
    }
}
