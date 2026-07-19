package com.sharkdom.model.referral;

import com.sharkdom.entity.referral.LeadsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class LeadsStats {
    private Date date;
    private String name;
    private String email;
    private LeadsEntity.LeadsStatus leadsStatus;
}
