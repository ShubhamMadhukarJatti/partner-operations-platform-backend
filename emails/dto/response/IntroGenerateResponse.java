package com.sharkdom.partnerattribution.emails.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IntroGenerateResponse {

    private String subject;

    private String body;

    @JsonProperty("word_count")
    private Integer wordCount;

    @JsonProperty("input_tokens")
    private Integer inputTokens;

    @JsonProperty("output_tokens")
    private Integer outputTokens;
}