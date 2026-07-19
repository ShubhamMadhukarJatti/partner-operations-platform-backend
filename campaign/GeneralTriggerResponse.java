package com.sharkdom.model.campaign;

import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.constants.campaign.TriggerStatus;
import com.sharkdom.entity.campaign.TriggerFlow;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GeneralTriggerResponse {
    private Long organizationId;
    private boolean sendAll;
    private List<PartnerDetail> activePartners;
    private List<PartnerDetail> assignedPartners;
    private TriggerStatus status;
    private String campaignName;
    private CampaignType campaignType;
    private TriggerFlow triggerFlow;
}
