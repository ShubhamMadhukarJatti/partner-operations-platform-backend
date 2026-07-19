package com.sharkdom.quickstart.entity;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.quickstart.dto.RewardType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_quick_start_reward_point_videos")
public class QuickStartRewardPointVideos extends BaseEntity {

    @Column(name = "video_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "thumbnail_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(nullable = false)
    private Long rewardPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, unique = true)
    private RewardType rewardType;
}
