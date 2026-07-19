package com.sharkdom.model.referral;

import com.sharkdom.entity.referral.LeadsEntity;

import java.time.LocalDate;

public interface LeadsInfo {
    LocalDate getDate();

    String getEmail();

    String getName();

    LeadsEntity.LeadsStatus getLeadStatus();
}
