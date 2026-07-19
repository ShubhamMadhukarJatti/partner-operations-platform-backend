package com.sharkdom.partnerattribution.service;


import com.sharkdom.partnerattribution.dto.PartnerMeetingIntroRequestDTO;

public interface PartnerMeetingIntroPromptService {

    String buildPrompt(PartnerMeetingIntroRequestDTO request);
}