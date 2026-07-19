package com.sharkdom.reseller.dto;

import com.sharkdom.reseller.entity.ResellerDealDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResellerDealStageResponse {
    private List<ResellerDealDetails> createdDeals;
    private List<ResellerDealDetails> receivedDeals;
}

