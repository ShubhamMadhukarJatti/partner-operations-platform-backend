package com.sharkdom.controller.catalogue;

import com.sharkdom.entity.catalogue.PricingTier;
import com.sharkdom.entity.catalogue.dto.*;
import com.sharkdom.service.catalogue.*;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/catalogues")
@RequiredArgsConstructor
@Slf4j
@Tag(name="Catalogue",description="Pricing Tier & Partner Tier APIs")
public class CatalogueRestController {

    private final PricingTierService pricingTierService;
    private final PartnerTierService partnerTierService;

    // ================= PRICING TIER =================
    @PostMapping("/pricing/tiers/add")
    @Operation(summary="Create Pricing Tier")
    public ResponseEntity<SharkdomApiResponse<PricingTierResponse>> createTier(@RequestBody CreatePricingTierRequest req){
        log.info("Create Pricing Tier | name={} | price={}",req.getTierName(),req.getPrice());
        PricingTierResponse res=pricingTierService.createPricingTier(req);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Pricing tier created successfully",res));
    }

    @DeleteMapping("/pricing/tiers/delete/{id}")
    @Operation(summary="Delete Pricing Tier")
    public ResponseEntity<SharkdomApiResponse<Void>> deleteTier(@PathVariable Long id){
        log.info("Delete Pricing Tier | id={}",id);
        pricingTierService.deletePricingTierById(id);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Pricing tier deleted successfully",null));
    }

    @PutMapping("/pricing/tiers/update/{id}")
    @Operation(summary="Update Pricing Tier")
    public ResponseEntity<SharkdomApiResponse<PricingTierResponse>> updateTier(@PathVariable Long id,@RequestBody UpdatePricingTierRequest req){
        log.info("Update Pricing Tier | id={}",id);
        PricingTierResponse res=pricingTierService.updatePricingTier(id,req);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Pricing tier updated successfully",res));
    }

    @PutMapping("/pricing/tiers/{id}/status")
    @Operation(summary="Update Pricing Tier Status")
    public ResponseEntity<SharkdomApiResponse<PricingTier>> updateTierStatus(@PathVariable Long id,@RequestBody UpdatePricingTierStatusRequest req){
        log.info("Update Pricing Tier Status | id={} | active={}",id,req.getActive());
        PricingTier tier=pricingTierService.updatePricingTierStatus(id,req.getActive());
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Pricing tier status updated successfully",tier));
    }

    @GetMapping("/pricing/tiers/details/{id}")
    @Operation(summary="Get Pricing Tier By Id")
    public ResponseEntity<SharkdomApiResponse<PricingTierResponse>> getTier(@PathVariable Long id){
        log.info("Get Pricing Tier | id={}",id);
        PricingTierResponse res=pricingTierService.getPricingTierById(id);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Pricing tier fetched successfully",res));
    }

    @GetMapping("/pricing/tiers")
    @Operation(summary="List Pricing Tiers")
    public ResponseEntity<SharkdomApiResponse<PricingTierListResponse>> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){
        log.info("List Pricing Tiers | page={} | size={}",page,size);
        PricingTierListResponse res=pricingTierService.getPricingTiersByOrgId(page,size);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,res.isHasData()?"Pricing tiers fetched successfully":"No pricing tiers found",res));
    }

    // ================= PARTNER TIER =================
    @PostMapping("/partner/tiers/add")
    @Operation(summary="Create Partner Tier")
    public ResponseEntity<SharkdomApiResponse<PartnerTierResponse>> createPartner(@RequestBody CreatePartnerTierRequest req){
        log.info("Create Partner Tier | name={}",req.getTierName());
        PartnerTierResponse res=partnerTierService.create(req);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Partner tier created successfully",res));
    }

    @GetMapping("/partner/tiers/details/{id}")
    @Operation(summary="Get Partner Tier By Id")
    public ResponseEntity<SharkdomApiResponse<PartnerTierResponse>> getPartner(@PathVariable Long id){
        log.info("Get Partner Tier | id={}",id);
        PartnerTierResponse res=partnerTierService.getById(id);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Partner tier fetched successfully",res));
    }

    @PutMapping("/partner/tiers/update/{id}")
    @Operation(summary="Update Partner Tier")
    public ResponseEntity<SharkdomApiResponse<PartnerTierResponse>> updatePartner(@PathVariable Long id,@RequestBody UpdatePartnerTierRequest req){
        log.info("Update Partner Tier | id={}",id);
        PartnerTierResponse res=partnerTierService.update(id,req);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Partner tier updated successfully",res));
    }

    @DeleteMapping("/partner/tiers/delete/{id}")
    @Operation(summary="Delete Partner Tier")
    public ResponseEntity<SharkdomApiResponse<Void>> deletePartner(@PathVariable Long id){
        log.info("Delete Partner Tier | id={}",id);
        partnerTierService.delete(id);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Partner tier deleted successfully",null));
    }

    @PutMapping("/partner/tiers/{id}/status")
    @Operation(summary="Update Partner Tier Status")
    public ResponseEntity<SharkdomApiResponse<Void>> updatePartnerStatus(@PathVariable Long id,@RequestBody UpdatePartnerTierStatusRequest req){
        log.info("Update Partner Tier Status | id={} | active={}",id,req.getActive());
        partnerTierService.updateStatus(id,req.getActive());
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Partner tier status updated successfully",null));
    }

    @GetMapping("/partner/tiers")
    @Operation(summary="List Partner Tiers")
    public ResponseEntity<SharkdomApiResponse<PartnerTierListResponse>> listPartner(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){
        log.info("List Partner Tiers | page={} | size={}",page,size);
        PartnerTierListResponse res=partnerTierService.listByOrg(page,size);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,res.isHasData()?"Partner tiers fetched successfully":"No partner tiers found",res));
    }

    // ================= COMBINED =================
    @GetMapping
    @Operation(summary="List Complete Catalogue")
    public ResponseEntity<SharkdomApiResponse<CatalogueCombinedListResponse>> listAll(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){
        log.info("List Catalogue | page={} | size={}",page,size);
        CatalogueCombinedListResponse res=partnerTierService.listCatalogue(page,size);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,res.isHasData()?"Catalogue data fetched successfully":"No catalogue data found",res));
    }
}