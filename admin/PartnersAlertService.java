package com.sharkdom.service.admin;

import com.sharkdom.constants.Days;
import com.sharkdom.entity.admin.PartnerAlertsEntity;
import com.sharkdom.repository.admin.PartnerAlertsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnersAlertService {

    private final PartnerAlertsRepository partnerAlertsRepository;

    public PartnersAlertService(PartnerAlertsRepository partnerAlertsRepository) {
        this.partnerAlertsRepository = partnerAlertsRepository;
    }

    public PartnerAlertsEntity disableAlert(Days days, boolean disable) {
        return partnerAlertsRepository.save(new PartnerAlertsEntity(days, disable));
    }

    public List<PartnerAlertsEntity> getAllAlerts() {
        return partnerAlertsRepository.findAll();
    }
}

