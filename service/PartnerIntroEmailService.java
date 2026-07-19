package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.PartnerIntroEmailRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class PartnerIntroEmailService {

    public String generatePartnerIntroPrompt(PartnerIntroEmailRequestDTO req) {

        return """
                Partner to Target Account (The Actual Intro)
                
                You are %s, %s at %s.
                
                Write a warm, credible 3-way introduction email to %s, %s at %s introducing %s, %s at %s.
                
                Context:
                
                Your relationship with %s:
                %s (%s ago)
                
                Why you are making this intro:
                %s
                
                What %s does:
                %s
                
                Why it is specifically relevant to %s right now:
                %s
                
                Tone of your relationship with target:
                %s
                
                Rules:
                - Maximum 120 words
                - Open addressing both parties in the same email
                - Establish credibility of BOTH people in 1-2 sentences each
                - Reason for intro must feel genuine
                - Do not use phrases like "I will let you take it from here"
                - End by stepping back naturally
                - Tone matches %s
                """.formatted(

                req.getPartnerContactName(),
                req.getPartnerContactTitle(),
                req.getPartnerCompany(),

                req.getTargetContactName(),
                req.getTargetContactTitle(),
                req.getTargetAccountName(),

                req.getSenderName(),
                req.getSenderTitle(),
                req.getSenderCompany(),

                req.getTargetAccountName(),
                req.getPartnerRelationshipType(),
                req.getPartnerRelationshipDuration(),

                req.getWhyMakingIntro(),

                req.getSenderCompany(),
                req.getSenderCompanyDescription(),

                req.getTargetAccountName(),
                req.getRelevanceToTarget(),

                req.getRelationshipTone(),
                req.getRelationshipTone()
        );
    }

}