package com.sharkdom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFlagResponse {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("is_continue_free_deal")
    private boolean isContinueFreeDeal;

    @JsonProperty("is_continue_free_partner_mapping")
    private boolean isContinueFreePartnerMapping;

}