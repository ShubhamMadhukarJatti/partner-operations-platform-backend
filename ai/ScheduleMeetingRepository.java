package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.ScheduleMeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleMeetingRepository extends JpaRepository<ScheduleMeetingEntity, Long> {
}
