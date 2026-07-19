package com.sharkdom.repository.common;

import com.sharkdom.model.common.StepTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StepTrackerRepository extends JpaRepository<StepTracker, Long> {

    Optional<StepTracker> findByUserId(String userId);
}
