package com.sharkdom.reseller.controller;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.reseller.dto.*;
import com.sharkdom.reseller.entity.*;
import com.sharkdom.reseller.service.ResellerService;
import com.sharkdom.reseller.service.VendorPaymentService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reseller Deal Controller
 *
 * Handles:
 * - Deal management
 * - Customer operations
 * - License allocation
 * - Approval workflow
 * - Vendor conflict validation
 * - Stripe payment integration
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/reseller/deals")
@Tag(name="Reseller Deal APIs",description="Manage reseller deals, customers, licenses & payments")
public class ResellerDealRestController {

    @Autowired private ResellerService service;
    @Autowired private VendorPaymentService vendorPaymentService;

    /** Create new reseller deal */
    @Operation(summary="Create Reseller Deal")
    @PostMapping("/create")
    public ResponseEntity<SharkdomApiResponse<ResellerDealDetailsResponse>> createDeal(@RequestBody ResellerDealDetailsRequest req){
        var res=service.createDeal(req);
        log.info("Deal created id={}",res.getId());
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Reseller deal created successfully",res));
    }

    /** Fetch deal details by ID */
    @Operation(summary="Get Deal Details")
    @GetMapping("/details/{id}")
    public ResponseEntity<SharkdomApiResponse<ResellerDealDetailsResponse>> getDeal(
            @Parameter(description="Deal ID",required=true) @PathVariable Long id){
        log.info("Fetch deal id={}",id);
        var res=service.getDealDetails(id);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Deal fetched successfully",res));
    }

    /** Calculate partner tier based on licenses */
    @Operation(summary="Calculate Partner Tier")
    @GetMapping("/calculate/partner/tier")
    public ResponseEntity<SharkdomApiResponse<PartnerTierCalculatedResponse>> calculateTier(
            @Parameter(description="Organization ID") @RequestParam Long orgId,
            @Parameter(description="Number of licenses") @RequestParam Long numberOfLicences){
        var res=service.getPartnerTierCalculation(orgId,numberOfLicences);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,res==null?"No tier found":"Tier calculated",res));
    }

    /** Get deal counts by status */
    @Operation(summary="Get Deal Status Count")
    @GetMapping("/count/status")
    public ResponseEntity<SharkdomApiResponse<List<ResellerDealStatusCountDto>>> getStatusCount(){
        Long orgId=Util.getOrgIdFromToken();
        log.info("Fetch deal counts orgId={}",orgId);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Status count fetched",service.getResellerDealCountByStatusForOrg()));
    }

    /** Get license details for deal */
    @Operation(summary="Get License Details")
    @GetMapping("/{dealId}/licenses")
    public ResponseEntity<SharkdomApiResponse<ResellerLicenseDetailsDto>> getLicenses(
            @Parameter(description="Deal ID") @PathVariable Long dealId){
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"License details fetched",service.getLicenseDetails(dealId)));
    }

    /** Get deals by stage */
    @Operation(summary="Get Deals By Stage")
    @GetMapping("/stage")
    public ResponseEntity<SharkdomApiResponse<ResellerDealStageResponse>> getByStage(
            @Parameter(description="Deal status") @RequestParam ResellerDealStatus status){
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Deals fetched",service.getDealsForStage(status)));
    }

    /** Add customer to deal */
    @Operation(summary="Add Deal Customer")
    @PostMapping("/customer/add")
    public ResponseEntity<SharkdomApiResponse<ResellerDealCustomerResponse>> addCustomer(@RequestBody ResellerDealCustomerRequest req){
        var res=service.addResellerDealCustomer(req);
        log.info("Customer added email={} dealId={}",req.getEmail(),req.getResellerDealId());
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Customer added successfully",res));
    }

    /** Get customer by ID */
    @Operation(summary="Get Customer By ID")
    @GetMapping("/customer/{id}")
    public ResponseEntity<SharkdomApiResponse<ResellerDealCustomerResponse>> getCustomer(
            @Parameter(description="Customer ID") @PathVariable Long id){
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Customer fetched",service.getCustomerById(id)));
    }

    /** Get customers with pagination */
    @Operation(summary="Get Customers By Deal")
    @GetMapping("/customer/list")
    public ResponseEntity<SharkdomApiResponse<Page<ResellerDealCustomerResponse>>> getCustomers(
            @RequestParam Long dealId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Customers fetched",service.getCustomerByDealId(dealId,page,size)));
    }

    /** Allocate license to customer */
    @Operation(summary="Allocate License")
    @PostMapping("/customer/license/allocate")
    public ResponseEntity<SharkdomApiResponse<LicenseAllocateResponse>> allocateLicense(@RequestBody LicenseAllocateRequest req){
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"License allocated",service.allocateLicense(req)));
    }

    /** Update customer */
    @Operation(summary="Update Customer")
    @PutMapping("/customer/{id}")
    public ResponseEntity<SharkdomApiResponse<ResellerDealCustomerResponse>> updateCustomer(@PathVariable Long id,@RequestBody ResellerDealCustomerRequest req){
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Customer updated",service.updateCustomer(id,req)));
    }

    /** Delete customer */
    @Operation(summary="Delete Customer")
    @DeleteMapping("/customer/{id}")
    public ResponseEntity<SharkdomApiResponse<Void>> deleteCustomer(@PathVariable Long id){
        service.deleteCustomer(id);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Customer deleted",null));
    }

    /** Approve or reject deal */
    @Operation(summary="Approve/Reject Deal")
    @PatchMapping("/approve/{dealId}")
    public ResponseEntity<SharkdomApiResponse<ResellerDealDetails>> approveDeal(
            @Parameter(description="Integration Type") @RequestParam IntegrationType integrationType,
            @Parameter(description="Approval status") @RequestParam Boolean isApproved,
            @PathVariable Long dealId){
        var res=service.updateApprovalStatus(dealId,isApproved,integrationType);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,isApproved?"Deal approved":"Deal rejected",res));
    }

    /** Validate vendor conflict before deal creation */
    @Operation(summary="Validate Vendor Conflict")
    @PostMapping("/validate-deal-conflict")
    public ResponseEntity<SharkdomApiResponse<Object>> validateConflict(
            @Parameter(description="Vendor Org ID") @RequestParam Long vendorOrgId){
        service.validateVendorConflict(Util.getOrgIdFromToken(),vendorOrgId);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"No conflict found",null));
    }

    /** Generate Stripe payment link */
    @Operation(summary="Generate Stripe Payment Link",description="Creates Stripe checkout session URL")
    @ApiResponses({
            @ApiResponse(responseCode="200",description="Payment link generated"),
            @ApiResponse(responseCode="404",description="Stripe config not found"),
            @ApiResponse(responseCode="500",description="Internal error")
    })
    @PostMapping("/stripe/payment/link/generation")
    public SharkdomApiResponse<String> generatePaymentLink(@Valid @RequestBody CreateCheckoutSessionRequest req){
        log.info("Generate payment link request");
        return new SharkdomApiResponse<>(true,"Payment link generated",vendorPaymentService.stripePaymentLinkGeneration(req));
    }

    /** Fetch reseller payment by requestId */
    @Operation(summary="Get Reseller Payment")
    @GetMapping("/stripe/payment/{requestId}")
    public SharkdomApiResponse<ResellerPayment> getPayment(
            @Parameter(description="Request ID") @PathVariable Long requestId){
        log.info("Fetch payment requestId={}",requestId);
        return new SharkdomApiResponse<>(true,"Payment fetched",vendorPaymentService.getPaymentByRequestId(requestId));
    }
}