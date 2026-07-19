package com.sharkdom.repository.campaign;

import com.sharkdom.constants.campaign.TriggerStatus;
import com.sharkdom.entity.campaign.GeneralTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralTriggerRepository extends JpaRepository<GeneralTrigger, Long> {
    List<GeneralTrigger> findAllByStatus(TriggerStatus status);
    List<GeneralTrigger> findAllByOrganizationId(long orgId);
}
