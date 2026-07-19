package com.sharkdom.repository.integration;

import com.sharkdom.entity.integration.EvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationEntityRepository extends JpaRepository<EvaluationEntity, Long> {
    List<EvaluationEntity> findAllByEmail(String email);
}
