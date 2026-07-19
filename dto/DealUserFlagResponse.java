package com.sharkdom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealUserFlagResponse {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("is_continue_free_deal")
    private boolean isContinueFreeDeal;


}