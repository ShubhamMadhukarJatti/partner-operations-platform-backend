package com.sharkdom.partnerattribution.service.impl;

import com.sharkdom.partnerattribution.dto.PartnerMeetingIntroRequestDTO;
import com.sharkdom.partnerattribution.service.PartnerMeetingIntroPromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;

@Service
@Slf4j
public class PartnerMeetingIntroPromptServiceImpl implements PartnerMeetingIntroPromptService {

    @Override
    public String buildPrompt(PartnerMeetingIntroRequestDTO request) {

        StringJoiner slots = new StringJoiner(" / ");
        if (request.getAvailableSlots() != null) {
            request.getAvailableSlots().forEach(slots::add);
        }

        String prompt = String.format(
                """
                You are %s, %s at %s.

                Write a short email to %s, %s at %s proposing a 3-way intro call with %s from %s.

                Context:

                - Your relationship with target:
                %s

                - What %s does:
                %s

                - Why you are suggesting the call:
                %s

                - Proposed agenda:
                %s

                - Duration:
                %s

                - Proposed slots from sender:
                %s

                - Level of your endorsement:
                %s

                Rules:
                - Maximum 130 words
                - Lead with the relationship
                - One sentence only explaining what %s does
                - Propose slots directly so target can respond in one email
                - Tone must match endorsement strength
                - Do not overpromise what the meeting will cover
                """,

                request.getPartnerContactName(),
                request.getPartnerContactTitle(),
                request.getPartnerCompany(),

                request.getTargetContactName(),
                request.getTargetContactTitle(),
                request.getTargetAccountName(),

                request.getSenderName(),
                request.getSenderCompany(),

                request.getPartnerRelationshipType(),

                request.getSenderCompany(),
                request.getSenderCompanyDescription(),

                request.getWhySuggesting(),

                request.getMeetingAgenda(),

                request.getMeetingDuration(),

                slots.toString(),

                request.getEndorsementStrength(),

                request.getSenderCompany()
        );

        log.info("Generated Partner Meeting Intro Prompt");

        return prompt;
    }
}