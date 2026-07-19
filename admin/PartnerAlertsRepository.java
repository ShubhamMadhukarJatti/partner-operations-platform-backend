package com.sharkdom.repository.admin;

import com.sharkdom.constants.Days;
import com.sharkdom.entity.admin.PartnerAlertsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerAlertsRepository extends JpaRepository<PartnerAlertsEntity, Long> {
    PartnerAlertsEntity findByDay(Days day);
}
