package com.sharkdom.service.catalogue;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.catalogue.*;
import com.sharkdom.entity.catalogue.dto.*;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.repository.catalogue.*;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingTierService {

    private final PricingTierRepository tierRepository;
    private final PricingFeatureRepository featureRepository;

    // ================= CREATE =================
    public PricingTierResponse createPricingTier(CreatePricingTierRequest req){
        Long orgId=Util.getOrgIdFromToken();
        log.info("Create tier | name={} | features={}",req.getTierName(),req.getFeatures()!=null?req.getFeatures().size():0);

        Set<PricingFeature> features=req.getFeatures().stream()
                .map(n->featureRepository.findByFeatureName(n)
                        .orElseGet(()->featureRepository.save(PricingFeature.builder().featureName(n).build())))
                .collect(Collectors.toSet());

        PricingTier tier=PricingTier.builder()
                .tierName(req.getTierName()).price(req.getPrice()).currency(req.getCurrency())
                .colorCode(req.getColorCode()).features(features).active(true).orgId(orgId).build();

        PricingTier saved=tierRepository.save(tier);

        return PricingTierResponse.builder()
                .id(saved.getId()).tierName(saved.getTierName()).price(saved.getPrice())
                .currency(saved.getCurrency()).colorCode(saved.getColorCode()).isActive(saved.getActive())
                .features(saved.getFeatures().stream().map(PricingFeature::getFeatureName).collect(Collectors.toSet()))
                .build();
    }

    // ================= DELETE =================
    public void deletePricingTierById(Long id){
        log.info("Delete tier | id={}",id);
        PricingTier t=tierRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Pricing Tier not found"));
        tierRepository.delete(t);
    }

    // ================= UPDATE =================
    public PricingTierResponse updatePricingTier(Long id,UpdatePricingTierRequest req){
        log.info("Update tier | id={}",id);
        PricingTier t=tierRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(ErrorMessages.valueOf("Pricing Tier not found")));

        if(req.getTierName()!=null) t.setTierName(req.getTierName());
        if(req.getPrice()!=null) t.setPrice(req.getPrice());
        if(req.getCurrency()!=null) t.setCurrency(req.getCurrency());
        if(req.getColorCode()!=null) t.setColorCode(req.getColorCode());

        if(req.getFeatures()!=null){
            Set<PricingFeature> f=req.getFeatures().stream()
                    .map(n->featureRepository.findByFeatureName(n)
                            .orElseGet(()->featureRepository.save(PricingFeature.builder().featureName(n).build())))
                    .collect(Collectors.toSet());
            t.setFeatures(f);
        }

        PricingTier updated=tierRepository.save(t);

        return PricingTierResponse.builder()
                .id(updated.getId()).tierName(updated.getTierName()).price(updated.getPrice())
                .currency(updated.getCurrency()).colorCode(updated.getColorCode()).isActive(updated.getActive())
                .features(updated.getFeatures().stream().map(PricingFeature::getFeatureName).collect(Collectors.toSet()))
                .build();
    }

    // ================= STATUS =================
    public PricingTier updatePricingTierStatus(Long id,Boolean active){
        log.info("Update status | id={} | active={}",id,active);
        PricingTier t=tierRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(ErrorMessages.valueOf("Pricing Tier not found")));
        t.setActive(active);
        return tierRepository.save(t);
    }

    // ================= GET =================
    public PricingTierResponse getPricingTierById(Long id){
        PricingTier t=tierRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(ErrorMessages.valueOf("Pricing Tier not found")));
        return PricingTierResponse.builder()
                .id(t.getId()).tierName(t.getTierName()).price(t.getPrice())
                .currency(t.getCurrency()).colorCode(t.getColorCode()).isActive(t.getActive())
                .features(t.getFeatures().stream().map(PricingFeature::getFeatureName).collect(Collectors.toSet()))
                .build();
    }

    // ================= LIST =================
    public PricingTierListResponse getPricingTiersByOrgId(int page,int size){
        Long orgId=Util.getOrgIdFromToken();
        Pageable pageable=PageRequest.of(page,size,Sort.by("id").descending());

        Page<PricingTier> pageRes=tierRepository.findByOrgId(orgId,pageable);
        boolean hasData=pageRes.hasContent();

        Page<PricingTierResponse> res=pageRes.map(t->PricingTierResponse.builder()
                .id(t.getId()).tierName(t.getTierName()).price(t.getPrice())
                .currency(t.getCurrency()).colorCode(t.getColorCode())
                .features(t.getFeatures().stream().map(PricingFeature::getFeatureName).collect(Collectors.toSet()))
                .build());

        return PricingTierListResponse.builder().hasData(hasData).plans(res).build();
    }


    // ================= LIST =================
    public PricingTierListResponse getPricingTiersByOrg(Long orgId,int page,int size){
        Pageable pageable=PageRequest.of(page,size,Sort.by("id").descending());

        Page<PricingTier> pageRes=tierRepository.findByOrgId(orgId,pageable);
        boolean hasData=pageRes.hasContent();

        Page<PricingTierResponse> res=pageRes.map(t->PricingTierResponse.builder()
                .id(t.getId()).tierName(t.getTierName()).price(t.getPrice())
                .currency(t.getCurrency()).colorCode(t.getColorCode())
                .features(t.getFeatures().stream().map(PricingFeature::getFeatureName).collect(Collectors.toSet()))
                .build());

        return PricingTierListResponse.builder().hasData(hasData).plans(res).build();
    }
}