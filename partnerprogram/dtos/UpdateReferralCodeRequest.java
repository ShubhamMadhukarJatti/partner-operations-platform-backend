package com.sharkdom.partnerprogram.dtos;

import lombok.Data;

@Data
public class UpdateReferralCodeRequest {

    private String email;
    private String referralCode;

}