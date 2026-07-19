package com.sharkdom.quickstart.service;

import com.sharkdom.quickstart.dto.RewardType;
import com.sharkdom.quickstart.entity.QuickStartRewardPointVideos;
import com.sharkdom.quickstart.repository.QuickStartRewardPointVideosRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Slf4j
@Service
public class QuickStartRewardPointVideosService {

    @Autowired
    private QuickStartRewardPointVideosRepository quickStartRewardPointVideosRepository;

    @Transactional
    public String saveOrUpdate(QuickStartRewardPointVideos request) {
        Optional<QuickStartRewardPointVideos> existing = quickStartRewardPointVideosRepository.findByRewardType(request.getRewardType());

        if (existing.isPresent()) {
            QuickStartRewardPointVideos existingVideo = existing.get();

            boolean updated = false;
            if (!existingVideo.getVideoUrl().equals(request.getVideoUrl())) {
                existingVideo.setVideoUrl(request.getVideoUrl());
                updated = true;
            }
            if (!existingVideo.getRewardPoint().equals(request.getRewardPoint())) {
                existingVideo.setRewardPoint(request.getRewardPoint());
                updated = true;
            }

            if (updated) {
                quickStartRewardPointVideosRepository.save(existingVideo);
                log.info("Updated video for RewardType: {} | ID: {} | New URL: {} | Points: {}",
                        request.getRewardType(), existingVideo.getId(),
                        existingVideo.getVideoUrl(), existingVideo.getRewardPoint());
                return "Video details updated successfully for reward type: " + request.getRewardType();
            } else {
                log.info("No changes detected for RewardType: {} | ID: {}",
                        request.getRewardType(), existingVideo.getId());
                return "No changes detected. Video already up-to-date for reward type: " + request.getRewardType();
            }
        } else {
            QuickStartRewardPointVideos newVideo = quickStartRewardPointVideosRepository.save(request);
            log.info("Created new video entry for RewardType: {} | ID: {} | URL: {} | Points: {}",
                    request.getRewardType(), newVideo.getId(),
                    newVideo.getVideoUrl(), newVideo.getRewardPoint());
            return "New video created successfully for reward type: " + request.getRewardType();
        }
    }

    public Optional<QuickStartRewardPointVideos> findByRewardType(RewardType rewardType) {
        return quickStartRewardPointVideosRepository.findByRewardType(rewardType);
    }
}
