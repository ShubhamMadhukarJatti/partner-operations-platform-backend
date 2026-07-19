package com.sharkdom.quickstart.controller;

import com.sharkdom.quickstart.dto.RewardType;
import com.sharkdom.quickstart.dto.RewardsOverviewResponse;
import com.sharkdom.quickstart.entity.QuickStartRewardPointVideos;
import com.sharkdom.quickstart.service.QuickStartRewardPointService;
import com.sharkdom.quickstart.service.QuickStartRewardPointVideosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quickstart/")
public class QuickStartRestController {

    @Autowired
    private QuickStartRewardPointVideosService quickStartRewardPointVideosService;

    @Autowired
    private QuickStartRewardPointService quickStartRewardPointService;

    @PostMapping("/video/url/upsert")
    public ResponseEntity<String> saveOrUpdate(@RequestBody QuickStartRewardPointVideos request) {
        log.info("Received request to save or update video: [RewardType={}, VideoURL={}, Points={}]",
                request.getRewardType(), request.getVideoUrl(), request.getRewardPoint());

        String message = quickStartRewardPointVideosService.saveOrUpdate(request);

        log.info("Request processed successfully: {}", message);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/detail/reward/{rewardType}")
    public ResponseEntity<Object> getByRewardType(@PathVariable RewardType rewardType) {
        log.info("Fetching video details for RewardType: {}", rewardType);

        return quickStartRewardPointVideosService.findByRewardType(rewardType)
                .<ResponseEntity<Object>>map(video -> {
                    log.info("Video found for RewardType {} -> ID: {}, URL: {}",
                            rewardType, video.getId(), video.getVideoUrl());
                    return ResponseEntity.ok(video);
                })
                .orElseGet(() -> {
                    log.warn("No video found for RewardType: {}", rewardType);
                    return ResponseEntity.status(404)
                            .body("No video found for reward type: " + rewardType);
                });
    }

    @PostMapping("/save/reward/{rewardType}")
    public ResponseEntity<Map<String, Object>> saveQuickStartDetails(@PathVariable RewardType rewardType) {
        Map<String, Object> response = quickStartRewardPointService.saveQuickStartDetails(rewardType);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/overview")
    public ResponseEntity<RewardsOverviewResponse> getRewardsOverview() {
        return ResponseEntity.ok(quickStartRewardPointService.getRewardsOverview());
    }


}
