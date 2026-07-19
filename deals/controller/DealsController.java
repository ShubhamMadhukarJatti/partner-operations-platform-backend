package com.sharkdom.deals.controller;

import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import com.sharkdom.deals.dto.CapturedPaymentResponse;
import com.sharkdom.deals.dto.CommissionRequestRequest;
import com.sharkdom.deals.dto.CommissionRequestResponse;
import com.sharkdom.deals.entity.CommissionRequestEntity;
import com.sharkdom.deals.entity.DealsEntity;
import com.sharkdom.deals.entity.DealsJoinerEntity;
import com.sharkdom.deals.model.*;
import com.sharkdom.deals.service.DealsService;
import com.sharkdom.entity.organization.IntegrationDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/deals")
public class DealsController {
    private final DealsService dealsService;

    public DealsController(DealsService dealsService) {
        this.dealsService = dealsService;
    }

    @Operation(summary = "Get all deals")
    @GetMapping("/all")
    public List<DealsResponse> getAll(@RequestParam Long organizationId) {
        return dealsService.getAllDeals(organizationId);
    }

    @Operation(summary = "Create new deal")
    @PostMapping("/create")
    public DealsEntity createDeal(@RequestBody CreateDealRequest createDealRequest) {
        return dealsService.createDeal(createDealRequest);
    }

    @Operation(summary = "Get my deal")
    @GetMapping("/my")
    public List<DealsResponse> getMyDeals(@RequestParam Long organizationId, @RequestParam DealType dealType) {
        return dealsService.getMyDeals(organizationId, dealType);
    }

    @Operation(summary = "Join deal")
    @PostMapping("/join")
    public DealsJoinerEntity joinDeal(@RequestBody JoinDealRequest request) {
        return dealsService.joinDeal(request);
    }

    @Operation(summary = "Connect Stripe")
    @PostMapping("/connectStripe")
    public IntegrationDetails connectStripeAccount(@RequestBody StripeConnectRequest request) {
        return dealsService.connectStripe(request);
    }

    @Operation(summary = "GET Deal Applications")
    @GetMapping("/applications")
    public List<DealJoinerResponse> getDealApplication(@RequestParam String dealId, @RequestParam(required = false) ApprovalRequestHistoryStatus status) {
        return dealsService.getDealApplication(dealId, status);
    }

    @Operation(summary = "GET Deal Details")
    @GetMapping("/details")
    public DealDetailsResponse getDealDetails(@RequestParam String dealId) {
        return dealsService.getDealDetails(dealId);
    }

    @Operation(summary = "Accept Deal Application")
    @PostMapping("/accept/application")
    public DealJoinerResponse acceptDealApplication(@RequestParam Long applicationId) {
        return dealsService.acceptDealApplication(applicationId);
    }

    @Operation(summary = "Reject an Application")
    @PostMapping("/reject/application")
    public DealJoinerResponse rejectDealApplication(@RequestParam Long applicationId) {
        return dealsService.rejectDealApplication(applicationId);
    }

    @Operation(summary = "End a deal")
    @PostMapping("/end")
    public DealsEntity endDeal(@RequestParam String dealId) {
        return dealsService.endDeal(dealId);
    }

    @Operation(summary = "Get affiliate link")
    @GetMapping("/affiliateLink")
    public DealAffiliateResponse getAffiliateLink(@RequestParam Long organizationId, @RequestParam String dealId) {
        return dealsService.getAffiliateLink(organizationId, dealId);
    }


    @Operation(summary = "Connect Razorpay")
    @PostMapping("/connectRazorpay")
    public IntegrationDetails connectRazorpay(@RequestBody StripeConnectRequest request) {
        return dealsService.connectRazorpay(request);
    }

    @Operation(summary = "Connect Bank Account")
    @PostMapping("/connectBankAccount")
    public IntegrationDetails connectRazorpay(@RequestBody BankAccountRequest request) {
        return dealsService.connectBank(request);
    }

    @Operation(summary = "Check Connected Accounts")
    @GetMapping("/connectedAccounts")
    public ConnectedAccountsResponse connectRazorpay(@RequestParam Long organizationId) {
        return dealsService.checkConnectedAccounts(organizationId);
    }

    @Operation(summary = "Add Money in walllet")
    @PostMapping("/addMoney")
    public AddMoneyResponse addMoney(@RequestBody AddMoneyRequest request) {
        return dealsService.addMoney(request);
    }

    @Operation(summary = "Payout")
    @PostMapping("/payout")
    public PayoutMoneyResponse payout(@RequestBody PayoutMoneyRequest request) {
        return dealsService.payout(request);
    }

    @Operation(summary = "Get payout summary")
    @GetMapping("/payout-summary")
    public Map<String, String> getPayoutSummary(@RequestParam Long orgId) {
        return dealsService.getPayoutSummary(orgId);
    }

    @Operation(summary = "Create Commission request")
    @PostMapping("/commission-request")
    public CommissionRequestEntity createCommissionRequest(@RequestBody CommissionRequestRequest request) {
        return dealsService.createCommissionRequest(request);
    }

    @Operation(summary = "Get Commission requests")
    @GetMapping("/commission-requests")
    public List<CommissionRequestResponse> getCommissionRequests(@RequestParam Long organizationId) {
        return dealsService.getCommissionRequests(organizationId);
    }

    @Operation(summary = "Accept commission request")
    @PostMapping("/commission-requests/{id}/accept")
    public CommissionRequestEntity acceptCommissionRequest(@PathVariable Long id) {
        return dealsService.acceptCommissionRequest(id);
    }

    @Operation(summary = "Reject commission request")
    @PostMapping("/commission-requests/{id}/reject")
    public CommissionRequestEntity rejectCommissionRequest(@PathVariable Long id, @RequestParam String rejectingReason) {
        return dealsService.rejectCommissionRequest(id, rejectingReason);
    }

    @Operation(summary = "Get Captured payments by organizationId")
    @GetMapping("/captured-payments")
    public List<CapturedPaymentResponse> getCapturedPayments(@RequestParam Long organizationId) {
        return dealsService.getCapturedPayments(organizationId);
    }
}
