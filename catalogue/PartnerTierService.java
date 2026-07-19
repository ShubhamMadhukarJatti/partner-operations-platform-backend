package com.sharkdom.service.catalogue;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.catalogue.*;
import com.sharkdom.entity.catalogue.dto.*;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.repository.catalogue.*;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PartnerTierService {

    @Autowired private PartnerTierRepository partnerTierRepository;
    @Autowired private PricingTierRepository pricingTierRepository;

    // ================= CREATE =================
    public PartnerTierResponse create(CreatePartnerTierRequest r){
        Long orgId=Util.getOrgIdFromToken();
        log.info("Create Partner Tier | name={}",r.getTierName());

        PartnerTier t=partnerTierRepository.save(
                PartnerTier.builder().orgId(orgId).tierName(r.getTierName()).price(r.getPrice())
                        .seatLower(r.getSeatLower()).seatUpper(r.getSeatUpper())
                        .discountPercent(r.getDiscountPercent()).region(r.getRegion())
                        .colorCode(r.getColorCode()).active(true).build());

        return map(t);
    }

    // ================= UPDATE =================
    public PartnerTierResponse update(Long id,UpdatePartnerTierRequest r){
        log.info("Update Partner Tier | id={}",id);
        PartnerTier t=find(id);

        if(r.getTierName()!=null) t.setTierName(r.getTierName());
        if(r.getPrice()!=null) t.setPrice(r.getPrice());
        if(r.getSeatLower()!=null) t.setSeatLower(r.getSeatLower());
        if(r.getSeatUpper()!=null) t.setSeatUpper(r.getSeatUpper());
        if(r.getDiscountPercent()!=null) t.setDiscountPercent(r.getDiscountPercent());
        if(r.getRegion()!=null) t.setRegion(r.getRegion());
        if(r.getColorCode()!=null) t.setColorCode(r.getColorCode());

        return map(partnerTierRepository.save(t));
    }

    // ================= GET =================
    public PartnerTierResponse getById(Long id){ return map(find(id)); }

    // ================= DELETE =================
    public void delete(Long id){ partnerTierRepository.delete(find(id)); log.info("Deleted Partner Tier | id={}",id); }

    // ================= STATUS =================
    public void updateStatus(Long id,Boolean active){
        PartnerTier t=find(id); t.setActive(active); partnerTierRepository.save(t);
        log.info("Updated status | id={} | active={}",id,active);
    }

    // ================= LIST =================
    public PartnerTierListResponse listByOrg(int page,int size){
        Long orgId=Util.getOrgIdFromToken();
        Pageable pageable=PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id"));

        Page<PartnerTier> pageData=partnerTierRepository.findByOrgId(orgId,pageable);

        return PartnerTierListResponse.builder()
                .hasData(pageData.hasContent())
                .tiers(pageData.map(this::map))
                .build();
    }

    // ================= FIND =================
    private PartnerTier find(Long id){
        return partnerTierRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(ErrorMessages.valueOf("Partner Tier not found")));
    }

    // ================= MAP PARTNER =================
    private PartnerTierResponse map(PartnerTier t){
        return PartnerTierResponse.builder()
                .id(t.getId()).tierName(t.getTierName()).price(t.getPrice())
                .seatLower(t.getSeatLower()).seatUpper(t.getSeatUpper())
                .discountPercent(t.getDiscountPercent()).region(t.getRegion())
                .colorCode(t.getColorCode()).active(t.getActive())
                .build();
    }

    // ================= COMBINED LIST =================
    public CatalogueCombinedListResponse listCatalogue(int page,int size){
        Long orgId=Util.getOrgIdFromToken();
        log.info("Fetch combined catalogue | orgId={} | page={} | size={}",orgId,page,size);

        Pageable pageable=PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id"));

        Page<PricingTierResponse> pricingPage=pricingTierRepository.findByOrgId(orgId,pageable).map(this::map);
        Page<PartnerTierResponse> partnerPage=partnerTierRepository.findByOrgId(orgId,pageable).map(this::map);

        boolean hasData=pricingPage.hasContent()||partnerPage.hasContent();

        return CatalogueCombinedListResponse.builder()
                .hasData(hasData)
                .pricingTiers(pricingPage)
                .partnerTiers(partnerPage)
                .build();
    }

    // ================= MAP PRICING =================
    private PricingTierResponse map(PricingTier t){
        return PricingTierResponse.builder()
                .id(t.getId()).tierName(t.getTierName()).price(t.getPrice())
                .currency(t.getCurrency()).colorCode(t.getColorCode())
                .features(t.getFeatures()!=null
                        ? t.getFeatures().stream().map(f->f.getFeatureName()).collect(Collectors.toSet())
                        : Set.of())
                .isActive(t.getActive())
                .build();
    }
}