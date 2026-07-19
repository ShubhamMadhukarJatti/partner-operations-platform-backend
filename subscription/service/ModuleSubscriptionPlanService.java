package com.sharkdom.subscription.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.subscription.entity.ModuleSubscriptionPlan;
import com.sharkdom.subscription.entity.UserSubscriptionPlanFreeTrail;
import com.sharkdom.subscription.model.*;
import com.sharkdom.subscription.repository.ModuleSubscriptionPlanFreeTrailRepository;
import com.sharkdom.subscription.repository.ModuleSubscriptionPlanRepository;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleSubscriptionPlanService {

    private final ModuleSubscriptionPlanRepository repository;
    private final ModuleSubscriptionPlanFreeTrailRepository freeTrailRepository;

    @Transactional
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> upsertSubscriptionPlan(
            ModuleSubscriptionPlanRequest request,Long orgId) {
        log.info("Starting UPSERT for subscription plan. orgId={}", orgId);


        if (request.getNumberOfSeats() == null || request.getNumberOfSeats() <= 0) {
            log.error("Invalid number of seats for orgId={}", orgId);
            throw new ServiceException(ErrorMessages.SH174);
        }

        boolean isUpdate = repository.findByOrgId(orgId).isPresent();

        ModuleSubscriptionPlan plan = repository
                .findByOrgId(orgId)
                .orElse(new ModuleSubscriptionPlan());

        plan.setOrgId(orgId);
        plan.setEmail(request.getEmail());
        plan.setNumberOfSeats(request.getNumberOfSeats());
        plan.setPrice(request.getPrice());
        plan.setOrganizationName(request.getOrganizationName());
        plan.setCountry(request.getCountry());
        plan.setAddress(request.getAddress());
        plan.setGstInId(request.getGstInId());

        ModuleSubscriptionPlan savedPlan = repository.save(plan);

        log.info("Subscription plan {} successfully for orgId={}",
                isUpdate ? "updated" : "created",
                orgId);

        ModuleSubscriptionPlanResponse response = ModuleSubscriptionPlanResponse.builder()
                .id(savedPlan.getId())
                .orgId(savedPlan.getOrgId())
                .numberOfSeats(savedPlan.getNumberOfSeats())
                .price(savedPlan.getPrice())
                .organizationName(savedPlan.getOrganizationName())
                .country(savedPlan.getCountry())
                .address(savedPlan.getAddress())
                .gstInId(savedPlan.getGstInId())
                .build();

        return new SharkdomApiResponse<>(
                true,
                isUpdate
                        ? "Subscription plan updated successfully"
                        : "Subscription plan created successfully",
                response
        );
    }

    @Transactional(readOnly = true)
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> getSubscriptionByOrgId(Long orgId) {

        log.info("Fetching subscription plan for orgId={}", orgId);

        if (orgId == null) {
            log.error("orgId is null while fetching subscription");
            throw new ServiceException(ErrorMessages.SH106);
        }

        ModuleSubscriptionPlan plan = repository.findByOrgId(orgId)
                .orElseThrow(() -> {
                    log.error("Subscription not found for orgId={}", orgId);
                    return new ServiceException(ErrorMessages.SH42, orgId);
                });

        ModuleSubscriptionPlanResponse response = ModuleSubscriptionPlanResponse.builder()
                .id(plan.getId())
                .email(plan.getEmail())
                .orgId(plan.getOrgId())
                .numberOfSeats(plan.getNumberOfSeats())
                .price(plan.getPrice())
                .organizationName(plan.getOrganizationName())
                .country(plan.getCountry())
                .address(plan.getAddress())
                .gstInId(plan.getGstInId())
                .build();

        log.info("Subscription fetched successfully for orgId={}", orgId);

        return new SharkdomApiResponse<>(
                true,
                "Subscription fetched successfully",
                response
        );
    }


    @Transactional
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> updateSubscriptionPlan(
            Long orgId,
            UpdateModuleSubscriptionPlanRequest request) {

        log.info("Starting UPDATE for subscription plan. orgId={}", orgId);

        ModuleSubscriptionPlan plan = repository.findByOrgId(orgId)
                .orElseThrow(() -> {
                    log.error("Subscription plan not found for orgId={}", orgId);
                    return new ServiceException(ErrorMessages.SH175);
                });


        if (request.getOrganizationName() != null) {
            plan.setOrganizationName(request.getOrganizationName());
        }
        if (request.getCountry() != null) {
            plan.setCountry(request.getCountry());
        }
        if (request.getAddress() != null) {
            plan.setAddress(request.getAddress());
        }
        if (request.getGstInId() != null) {
            plan.setGstInId(request.getGstInId());
        }

        ModuleSubscriptionPlan updatedPlan = repository.save(plan);

        log.info("Subscription plan updated successfully for orgId={}", orgId);

        ModuleSubscriptionPlanResponse response = ModuleSubscriptionPlanResponse.builder()
                .id(updatedPlan.getId())
                .orgId(updatedPlan.getOrgId())
                .numberOfSeats(updatedPlan.getNumberOfSeats())
                .price(updatedPlan.getPrice())
                .organizationName(updatedPlan.getOrganizationName())
                .country(updatedPlan.getCountry())
                .address(updatedPlan.getAddress())
                .gstInId(updatedPlan.getGstInId())
                .build();

        return new SharkdomApiResponse<>(
                true,
                "Subscription plan updated successfully",
                response
        );
    }

    @Transactional
    public SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> upsertFreeTrialSubscriptionPlan(
            UserSubscriptionPlanFreeTrailRequest request, String userEmail) {

        log.info("Starting UPSERT for free trial subscription plan. userEmail={}", userEmail);

        if (request.getNumberOfSeats() == null || request.getNumberOfSeats() <= 0) {
            log.error("Invalid number of seats for userEmail={}", userEmail);
            throw new ServiceException(ErrorMessages.SH174);
        }

        boolean isUpdate = freeTrailRepository.findByUserEmail(userEmail).isPresent();

        UserSubscriptionPlanFreeTrail plan = freeTrailRepository
                .findByUserEmail(userEmail)
                .orElse(new UserSubscriptionPlanFreeTrail());

        plan.setUserEmail(userEmail);
        plan.setNumberOfSeats(request.getNumberOfSeats());
        plan.setPrice(request.getPrice());
        plan.setOrganizationName(request.getOrganizationName());
        plan.setCountry(request.getCountry());
        plan.setAddress(request.getAddress());
        plan.setGstInId(request.getGstInId());

        UserSubscriptionPlanFreeTrail savedPlan = freeTrailRepository.save(plan);

        log.info("Free trial subscription plan {} successfully for userEmail={}",
                isUpdate ? "updated" : "created",
                userEmail);

        UserSubscriptionPlanFreeTrailResponse response = UserSubscriptionPlanFreeTrailResponse.builder()
                .id(savedPlan.getId())
                .userEmail(savedPlan.getUserEmail())
                .numberOfSeats(savedPlan.getNumberOfSeats())
                .price(savedPlan.getPrice())
                .organizationName(savedPlan.getOrganizationName())
                .country(savedPlan.getCountry())
                .address(savedPlan.getAddress())
                .gstInId(savedPlan.getGstInId())
                .build();

        return new SharkdomApiResponse<>(
                true,
                isUpdate
                        ? "Free trial subscription plan updated successfully"
                        : "Free trial subscription plan created successfully",
                response
        );
    }

    public SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> getFreeTrialSubscriptionPlanByEmail(
            String userEmail) {

        log.info("Fetching free trial subscription plan for userEmail={}", userEmail);

        UserSubscriptionPlanFreeTrail plan = freeTrailRepository
                .findByUserEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Free trial subscription plan not found for userEmail={}", userEmail);
                    return new ServiceException(ErrorMessages.SH174);
                });

        UserSubscriptionPlanFreeTrailResponse response =
                UserSubscriptionPlanFreeTrailResponse.builder()
                        .id(plan.getId())
                        .userEmail(plan.getUserEmail())
                        .numberOfSeats(plan.getNumberOfSeats())
                        .price(plan.getPrice())
                        .organizationName(plan.getOrganizationName())
                        .country(plan.getCountry())
                        .address(plan.getAddress())
                        .gstInId(plan.getGstInId())
                        .build();

        log.info("Free trial subscription plan fetched successfully for userEmail={}", userEmail);

        return new SharkdomApiResponse<>(
                true,
                "Free trial subscription plan fetched successfully",
                response
        );
    }


    @Transactional
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> convertFreeTrialToPaidPlan(
            String email) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Starting conversion from free trial to paid plan. email={}, orgId={}",
                email, orgId);

        if (orgId == null) {
            log.error("OrgId is null while converting free trial plan");
            throw new ServiceException(ErrorMessages.SH106);
        }

        UserSubscriptionPlanFreeTrail freeTrialPlan = freeTrailRepository
                .findByUserEmail(email)
                .orElseThrow(() -> {
                    log.error("Free trial subscription plan not found for email={}", email);
                    return new ServiceException(ErrorMessages.SH106);
                });

        boolean isUpdate = repository.findByOrgId(orgId).isPresent();

        ModuleSubscriptionPlan modulePlan = repository
                .findByOrgId(orgId)
                .orElse(new ModuleSubscriptionPlan());

        modulePlan.setOrgId(orgId);
        modulePlan.setEmail(freeTrialPlan.getUserEmail());
        modulePlan.setNumberOfSeats(freeTrialPlan.getNumberOfSeats());
        modulePlan.setPrice(freeTrialPlan.getPrice());
        modulePlan.setOrganizationName(freeTrialPlan.getOrganizationName());
        modulePlan.setCountry(freeTrialPlan.getCountry());
        modulePlan.setAddress(freeTrialPlan.getAddress());
        modulePlan.setGstInId(freeTrialPlan.getGstInId());

        ModuleSubscriptionPlan savedPlan = repository.save(modulePlan);

        // Optional: remove free trial record after successful conversion
        freeTrailRepository.delete(freeTrialPlan);

        log.info("Free trial converted successfully to module subscription. email={}, orgId={}",
                email, orgId);

        ModuleSubscriptionPlanResponse response = ModuleSubscriptionPlanResponse.builder()
                .id(savedPlan.getId())
                .orgId(savedPlan.getOrgId())
                .email(savedPlan.getEmail())
                .numberOfSeats(savedPlan.getNumberOfSeats())
                .price(savedPlan.getPrice())
                .organizationName(savedPlan.getOrganizationName())
                .country(savedPlan.getCountry())
                .address(savedPlan.getAddress())
                .gstInId(savedPlan.getGstInId())
                .build();

        return new SharkdomApiResponse<>(
                true,
                isUpdate
                        ? "Free trial converted and subscription updated successfully"
                        : "Free trial converted and subscription created successfully",
                response
        );
    }


    @Transactional
    public SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> updateFreeTrialSubscriptionPlanByEmail(
            UserSubscriptionPlanFreeTrailRequest request,
            String userEmail) {

        log.info("Starting UPDATE for free trial subscription plan. userEmail={}", userEmail);

        UserSubscriptionPlanFreeTrail plan = freeTrailRepository
                .findByUserEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Free trial subscription plan not found for userEmail={}", userEmail);
                    return new ServiceException(ErrorMessages.SH174);
                });

        if (request.getNumberOfSeats() != null) {
            plan.setNumberOfSeats(request.getNumberOfSeats());
        }

        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }

        if (request.getOrganizationName() != null) {
            plan.setOrganizationName(request.getOrganizationName());
        }

        if (request.getCountry() != null) {
            plan.setCountry(request.getCountry());
        }

        if (request.getAddress() != null) {
            plan.setAddress(request.getAddress());
        }

        if (request.getGstInId() != null) {
            plan.setGstInId(request.getGstInId());
        }

        UserSubscriptionPlanFreeTrail updatedPlan = freeTrailRepository.save(plan);

        log.info("Free trial subscription plan updated successfully for userEmail={}", userEmail);

        UserSubscriptionPlanFreeTrailResponse response =
                UserSubscriptionPlanFreeTrailResponse.builder()
                        .id(updatedPlan.getId())
                        .userEmail(updatedPlan.getUserEmail())
                        .numberOfSeats(updatedPlan.getNumberOfSeats())
                        .price(updatedPlan.getPrice())
                        .organizationName(updatedPlan.getOrganizationName())
                        .country(updatedPlan.getCountry())
                        .address(updatedPlan.getAddress())
                        .gstInId(updatedPlan.getGstInId())
                        .build();

        return new SharkdomApiResponse<>(
                true,
                "Free trial subscription plan updated successfully",
                response
        );
    }
}
