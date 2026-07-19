package com.sharkdom.model.campaign;

import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.constants.campaign.TriggerStatus;
import com.sharkdom.model.integration.TriggerFlowRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralTriggerRequest {
    @NotNull
    private Long organizationId;
    private boolean sendAll;
    private List<Long> partnerIds;
    @NotNull
    private TriggerStatus status;
    @NotNull
    private CampaignType campaignType;
    @NotEmpty
    private String campaignName;
    private TriggerFlowRequest triggerFlow;
}
