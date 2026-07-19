package com.sharkdom.model.referral;

import com.sharkdom.entity.referral.LeadsEntity;

public record UpdateLeadStatus(String email, String referralCode, LeadsEntity.LeadsStatus leadsStatus) {
}
