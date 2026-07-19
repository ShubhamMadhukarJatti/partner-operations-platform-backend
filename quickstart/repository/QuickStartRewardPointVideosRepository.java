package com.sharkdom.quickstart.repository;

import com.sharkdom.quickstart.dto.RewardType;
import com.sharkdom.quickstart.entity.QuickStartRewardPointVideos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuickStartRewardPointVideosRepository extends JpaRepository<QuickStartRewardPointVideos, Long> {

    Optional<QuickStartRewardPointVideos> findByRewardType(RewardType rewardType);

    boolean existsByRewardType(RewardType rewardType);
}
