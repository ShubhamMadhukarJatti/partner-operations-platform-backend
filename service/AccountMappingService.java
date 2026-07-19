package com.sharkdom.partnerattribution.service;


import com.sharkdom.partnerattribution.dto.AccountMappingSummaryDTO;
import com.sharkdom.partnerattribution.dto.CoSellRecommendationResponseDTO;
import com.sharkdom.partnerattribution.dto.SharedAccountDTO;
import com.sharkdom.util.SharkdomPaginatedResponse;

public interface AccountMappingService {

    AccountMappingSummaryDTO getAccountMappingSummary();

    SharkdomPaginatedResponse<SharedAccountDTO> getSharedAccounts(
            int page,
            int size,
            String sort,
            String filter,
            String search
    );

    CoSellRecommendationResponseDTO getCoSellRecommendation(String accountId);

}