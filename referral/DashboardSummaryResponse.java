package com.sharkdom.model.referral;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private SummarySection summary;
    private List<ActivePartnerDto> activePartners;
}
