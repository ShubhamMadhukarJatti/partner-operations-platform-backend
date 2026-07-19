package com.sharkdom.emailOutreach.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailBoxClaimCheckResponse {

    @JsonProperty("is_claimed")
    private boolean isClaimed;
}
