package com.sharkdom.repository.ai;

import com.sharkdom.model.ai.OverlapCronExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverlapCronExecutionRepository
        extends JpaRepository<OverlapCronExecutionEntity, Long> {
}