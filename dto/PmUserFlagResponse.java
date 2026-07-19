package com.sharkdom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PmUserFlagResponse {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("is_continue_free_partner_mapping")
    private boolean isContinueFreePartnerMapping;

}