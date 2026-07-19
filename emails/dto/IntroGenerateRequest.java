package com.sharkdom.partnerattribution.emails.dto;

import com.sharkdom.partnerattribution.emails.enums.IntroType;
import lombok.Data;

@Data
public class IntroGenerateRequest {

    private IntroType type;

    private IntroPayloadRequest data;
}