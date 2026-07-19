package com.sharkdom.model.campaign;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampaignResponse extends Campaign {
    private Long organizationId;
    private List<PartnerDetail> activePartners;
}
