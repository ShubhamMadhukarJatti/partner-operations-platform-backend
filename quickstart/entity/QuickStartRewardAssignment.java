package com.sharkdom.quickstart.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.quickstart.dto.RewardType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "t_quick_start_reward_assignment")
@EqualsAndHashCode(callSuper = true)
public class QuickStartRewardAssignment extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 50)
    private RewardType rewardType;

}