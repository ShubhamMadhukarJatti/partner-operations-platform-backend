package com.sharkdom.repository.campaign;

import com.sharkdom.entity.campaign.TriggerTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerTemplateRepository extends JpaRepository<TriggerTemplate, Long> {
    List<TriggerTemplate> findAllByUserId(String userId);
}
