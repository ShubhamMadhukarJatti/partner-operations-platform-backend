package com.sharkdom.entity.campaign;

import com.sharkdom.constants.campaign.CampaignType;
import com.sharkdom.constants.campaign.TriggerStatus;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "general_triggers")
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralTrigger extends BaseEntity {
    private Long organizationId;
    private boolean sendAll;
    private List<Long> partnerIds;
    private TriggerStatus status;
    private String campaignName;
    private CampaignType campaignType;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "trigger_flow_id")
    private TriggerFlow triggerFlow;
}
