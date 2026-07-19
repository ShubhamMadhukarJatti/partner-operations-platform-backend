package com.sharkdom.model.referral;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivePartnerDto {
    private Long id;
    private String name;
    private String status;
    private String website;
    private String email;
    private int performance;
    private int referrals;
    private Integer impressions;
}