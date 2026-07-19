package com.sharkdom.model.referral;

import com.sharkdom.entity.referral.CampaignEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CampaignData {
   private List<CampaignEntity> campaignEntityList;

}
