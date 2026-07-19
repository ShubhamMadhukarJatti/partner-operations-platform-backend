package com.sharkdom.model.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompanyDetails {
    private List<PercentageCategory> isPartnershipProgram;
    private List<PercentageCategory> companySector;
    private List<PercentageCategory> companySize;
    private List<PercentageCategory> marketSegment;
}
