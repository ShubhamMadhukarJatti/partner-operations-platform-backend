package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.ModeSaveEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeSaveRepository extends JpaRepository<ModeSaveEntity, Long> {
    Page<ModeSaveEntity> findByEntity(String entity, Pageable pageable);
}
