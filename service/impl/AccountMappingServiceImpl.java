package com.sharkdom.partnerattribution.service.impl;

import com.sharkdom.partnerattribution.dto.*;
import com.sharkdom.partnerattribution.service.AccountMappingService;
import com.sharkdom.util.SharkdomPaginatedResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountMappingServiceImpl implements AccountMappingService {

    @Override
    public AccountMappingSummaryDTO getAccountMappingSummary() {

        AccountMappingSummaryDTO response = new AccountMappingSummaryDTO();

        response.setYourAccounts(847);
        response.setPartnerAccounts(1240);
        response.setSharedAccounts(213);

        response.setOnlyYou(634);
        response.setOnlyPartner(1027);

        OverlapCategoriesDTO categories = new OverlapCategoriesDTO();
        categories.setHighPriority(47);
        categories.setCosellReady(98);
        categories.setMonitor(68);

        response.setOverlapCategories(categories);
        response.setLastSynced(Instant.parse("2026-04-04T10:23:12Z"));

        return response;
    }

    @Override
    public SharkdomPaginatedResponse<SharedAccountDTO> getSharedAccounts(
            int page,
            int size,
            String sort,
            String filter,
            String search) {

        List<SharedAccountDTO> accounts = new ArrayList<>();


        SharedAccountDTO acc1 = new SharedAccountDTO();
        acc1.setName("Bektike Infotech");
        acc1.setDomain("bektikeinfotech.com");
        acc1.setOverlapType("HOT");
        acc1.setOpportunityScore(94);
        acc1.setYourStage("PROPOSAL_SENT");
        acc1.setPartnerStage("CLOSED_CUSTOMER");
        acc1.setEstimatedACV(124000);
        acc1.setRecommendedAction("START_COSELL");
        acc1.setCurrentPartnerDealId("86765323243");
        acc1.setTargetPartnerDealId("88436764367");
        acc1.setTargetPartnerDealOwnerId("65436754367");
        acc1.setCurrentPartnerDealOwnerId("65436798367");

        SharedAccountDTO acc2 = new SharedAccountDTO();
        acc2.setName("Vatoro Pay");
        acc2.setDomain("vatoropay.com");
        acc2.setOverlapType("COSELL_READY");
        acc2.setOpportunityScore(76);
        acc2.setYourStage("DISCOVERY");
        acc2.setPartnerStage("CLOSED_CUSTOMER");
        acc2.setEstimatedACV(67500);
        acc2.setRecommendedAction("REQUEST_INTRO");
        acc2.setCurrentPartnerDealId("97345218764");
        acc2.setTargetPartnerDealId("78654321987");
        acc2.setTargetPartnerDealOwnerId("54321678901");
        acc2.setCurrentPartnerDealOwnerId("65478932145");



        SharedAccountDTO acc3 = new SharedAccountDTO();
        acc3.setName("GlobalPay Inc");
        acc3.setDomain("globalpay.com");
        acc3.setOverlapType("COSELL_READY");
        acc3.setOpportunityScore(76);
        acc3.setYourStage("DISCOVERY");
        acc3.setPartnerStage("CLOSED_CUSTOMER");
        acc3.setEstimatedACV(67500);
        acc3.setRecommendedAction("ADD_TO_PIPELINE");
        acc3.setCurrentPartnerDealId("65432198745");
        acc3.setTargetPartnerDealId("91827364550");
        acc3.setTargetPartnerDealOwnerId("77889966554");
        acc3.setCurrentPartnerDealOwnerId("11223344556");


        accounts.add(acc1);
        accounts.add(acc2);
        accounts.add(acc3);

        SharkdomPaginatedResponse<SharedAccountDTO> response = new SharkdomPaginatedResponse<>();
        response.setContent(accounts);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(accounts.size());
        response.setTotalPages(1);
        response.setLast(true);

        return response;
    }


    public CoSellRecommendationResponseDTO getCoSellRecommendation(String accountId) {

        CoSellRecommendationResponseDTO response = new CoSellRecommendationResponseDTO();

        response.setAccountId(accountId);
        response.setAccountName("Workomo Pvt Ltd");
        response.setStage("DEMO_SCHEDULED");

        RecommendationDTO recommendation = new RecommendationDTO();
        recommendation.setType("CO_SELL");
        recommendation.setConfidenceScore(0.87);
        recommendation.setMessage("Partner collaboration can increase win probability");

        response.setRecommendation(recommendation);

        InsightDTO insight1 = new InsightDTO();
        insight1.setId("ins_1");
        insight1.setType("PARTNER_HISTORY");
        insight1.setText("Partner closed a similar account (GlobalPay) last quarter");

        InsightDTO insight2 = new InsightDTO();
        insight2.setId("ins_2");
        insight2.setType("PERFORMANCE_METRIC");
        insight2.setText("Co-sell deals close 38% faster than direct at Demo Scheduled stage");

        response.setInsights(List.of(insight1, insight2));

        ActionDTO cancel = new ActionDTO();
        cancel.setType("CANCEL");
        cancel.setLabel("Cancel");

        ActionDTO viewAccount = new ActionDTO();
        viewAccount.setType("VIEW_ACCOUNT");
        viewAccount.setLabel("View Account Details");
        viewAccount.setRedirectUrl("/accounts/" + accountId);

        ActionDTO initiate = new ActionDTO();
        initiate.setType("INITIATE_CO_SELL");
        initiate.setLabel("Initiate Co-sell");
        initiate.setApi("/api/v1/co-sell/initiate");
        initiate.setMethod("POST");

        response.setActions(List.of(cancel, viewAccount, initiate));

        response.setCreatedAt(Instant.parse("2026-04-07T10:30:00Z"));

        return response;
    }

}