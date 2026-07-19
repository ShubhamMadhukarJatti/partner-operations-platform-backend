package com.sharkdom.controller.organization;

import com.sharkdom.dto.*;
import com.sharkdom.entity.organization.GettingStartedEntity;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.stripe.StripeInvoice;
import com.sharkdom.service.organization.SettingSectionService;
import com.sharkdom.util.SharkdomApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/settings/sections")
public class SettingSectionRestController {

    @Autowired
    private SettingSectionService settingSectionService;

    @PutMapping("/update/company/details")
    public ResponseEntity<SharkdomApiResponse<?>> updateCompanyDetails(
            @RequestBody CompanyDetailsRequest request
    ) {
        log.info("updateOrganization>> Request: {}", request);
        Organization updatedOrg = settingSectionService.updateCompanyDetails(request);
        log.info("updateOrganization>> Updated Organization ID: {}", updatedOrg.getId());
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Organization updated successfully",
                        updatedOrg.getId()
                )
        );
    }

    @GetMapping("/get/company/details")
    public ResponseEntity<SharkdomApiResponse<?>> getCompanyDetails() {
        log.info("getOrganization>> Fetching company details");
        CompanyDetailsResponse details = settingSectionService.getCompanyDetails();
        log.info("getOrganization>> Organization details fetched");
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Organization details fetched successfully",
                        details
                )
        );
    }

    @PutMapping("/update/partnership/details")
    public ResponseEntity<SharkdomApiResponse<?>> updatePartnershipDetails(
            @RequestBody PartnershipUpdateRequest request
    ) {
        log.info("updateOrganization>> Request: {}", request);
        Organization updatedOrg = settingSectionService.updatePartnershipDetails(request);
        log.info("updateOrganization>> Updated Organization ID: {}", updatedOrg.getId());
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Organization updated successfully",
                        updatedOrg.getId()
                )
        );
    }

    @GetMapping("/get/partnership/details")
    public ResponseEntity<SharkdomApiResponse<?>> getPartnershipDetails() {
        log.info("Fetching partnership details API");
        PartnershipDetailsResponse response = settingSectionService.getPartnershipDetails();
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Partnership details fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/update/address/contact")
    public ResponseEntity<SharkdomApiResponse<?>> updateAddressAndContact(
            @RequestBody AddressContactUpdateRequest request
    ) {
        log.info("Update Address & Contact | {}", request);
        Organization org = settingSectionService.updateAddressAndContact(request);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Address & Contact updated successfully",
                        org.getId()
                )
        );
    }

    @GetMapping("/get/address/contact")
    public ResponseEntity<SharkdomApiResponse<?>> getAddressAndContact() {
        AddressContactResponse response = settingSectionService.getAddressAndContact();
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Address & Contact fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/update/Ipp/details")
    public ResponseEntity<SharkdomApiResponse<?>> updateGettingStarted(
            @RequestBody GettingStartedUpdateRequest request
    ) {
        log.info("Update Getting Started Request: {}", request);
        GettingStartedEntity updated = settingSectionService.updateIppDetails(request);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Getting Started updated successfully",
                        updated.getId()
                )
        );
    }

    @GetMapping("/get/Ipp/details")
    public ResponseEntity<SharkdomApiResponse<?>> getGettingStarted() {
        GettingStartedResponse response = settingSectionService.getIPPDetails();
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Getting Started fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/get/payment/invoice/history")
    public ResponseEntity<SharkdomApiResponse<?>> getInvoicePaymentHistory() {
        log.info("Fetching Invoice History");
        var invoicesByOrg = settingSectionService.getInvoicesByOrg();
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Invoice history fetched successfully",
                        invoicesByOrg
                )
        );
    }




}
