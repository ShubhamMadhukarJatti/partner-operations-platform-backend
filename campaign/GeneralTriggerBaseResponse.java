package com.sharkdom.model.campaign;

import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.constants.campaign.TriggerStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class GeneralTriggerBaseResponse {
    private Long id;
    private Long organizationId;
    private TriggerStatus status;
    private String campaignName;
    private CampaignType campaignType;
    private Date creationTime;
}
