package com.sharkdom.partnerattribution.emails.service;

import com.sharkdom.partnerattribution.emails.dto.IntroGenerateRequest;
import com.sharkdom.partnerattribution.emails.dto.response.IntroGenerateResponse;

public interface IntroService {
    IntroGenerateResponse generateIntro(IntroGenerateRequest request);
}