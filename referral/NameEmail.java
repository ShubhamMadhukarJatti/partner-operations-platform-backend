package com.sharkdom.model.referral;

import com.sharkdom.entity.referral.LeadsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class NameEmail {
    String name;
    String email;
    LeadsEntity.LeadsStatus leadsStatus;
}
