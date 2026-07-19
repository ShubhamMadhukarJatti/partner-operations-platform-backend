package com.sharkdom.reseller.utill;

import com.sharkdom.reseller.dto.ResellerDealDetailsRequest;
import com.sharkdom.reseller.dto.ResellerDealDetailsResponse;
import org.springframework.stereotype.Component;

@Component
public class ResellerDealDetailsMapper {

    public com.sharkdom.reseller.entity.ResellerDealDetails toEntity(ResellerDealDetailsRequest request) {
        com.sharkdom.reseller.entity.ResellerDealDetails entity = new com.sharkdom.reseller.entity.ResellerDealDetails();
        entity.setExpectedReleaseTime(request.getExpectedReleaseTime());
        entity.setExpectedReleaseDate(request.getExpectedReleaseDate());
        entity.setResellerMode(request.getResellerMode());
        entity.setProductPlanRequired(request.getProductPlanRequired());
        entity.setNumberOfLicences(request.getNumberOfLicences());
        entity.setCalculatedPartnerTier(request.getCalculatedPartnerTier());
        entity.setBillingModel(request.getBillingModel());
        entity.setActualPrice(request.getActualPrice());
        entity.setBuyPrice(request.getBuyPrice());
        return entity;
    }

    public ResellerDealDetailsResponse toResponse(com.sharkdom.reseller.entity.ResellerDealDetails entity) {
        ResellerDealDetailsResponse response = new ResellerDealDetailsResponse();
        response.setId(entity.getId());
        response.setResellerOrgId(entity.getResellerOrgId());
        response.setVendorOrgId(entity.getVendorOrgId());
        response.setPartnerName(entity.getPartnerName());
        response.setExpectedReleaseTime(entity.getExpectedReleaseTime());
        response.setExpectedReleaseDate(entity.getExpectedReleaseDate());
        response.setResellerMode(entity.getResellerMode());
        response.setProductPlanRequired(entity.getProductPlanRequired());
        response.setNumberOfLicences(entity.getNumberOfLicences());
        response.setCalculatedPartnerTier(entity.getCalculatedPartnerTier());
        response.setBillingModel(entity.getBillingModel());
        response.setActualPrice(entity.getActualPrice());
        response.setBuyPrice(entity.getBuyPrice());
        response.setPoc(entity.getPoc());
        response.setResellerDealStatus(entity.getResellerDealStatus());
        response.setResellerDealStag(entity.getResellerDealStag());
        response.setResellerDealSource(entity.getResellerDealSource());
        return response;
    }
}