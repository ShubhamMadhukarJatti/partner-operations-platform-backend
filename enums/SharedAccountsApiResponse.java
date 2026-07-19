package com.sharkdom.partnerattribution.enums;

import com.sharkdom.partnerattribution.dto.SharedAccountResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SharedAccountsApiResponse {

    private Integer totalAccounts;
    private List<SharedAccountResponse> accounts;
}
