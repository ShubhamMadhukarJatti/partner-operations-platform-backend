package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.VendorMeetingIntroRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class VendorMeetingIntroService {

    public String generateVendorMeetingPrompt(VendorMeetingIntroRequestDTO req) {

        return """
                Vendor to Partner (Intro Request – Meeting)
                
                You are %s, %s at %s.
                
                Write an email to %s, %s at %s asking them to set up a 3-way intro call with %s at %s.
                
                Context:
                
                Partner's relationship with account:
                %s
                
                Relationship duration:
                %s
                
                Why a meeting over email:
                %s
                
                Proposed meeting duration:
                %s
                
                Proposed meeting agenda:
                %s
                
                Your available slots:
                %s
                
                What you are asking partner to do:
                Host the calendar invite so it comes from a trusted name.
                
                Rules:
                - Maximum 150 words
                - Acknowledge this is a higher ask than an email intro
                - Explain why a meeting makes sense for this account
                - Provide agenda and duration upfront
                - Offer to prepare a 1-page brief for partner
                - End with available slots
                - Tone respectful of partner's time and confident
                """.formatted(

                req.getSenderName(),
                req.getSenderTitle(),
                req.getSenderCompany(),

                req.getPartnerContactName(),
                req.getPartnerContactTitle(),
                req.getPartnerCompany(),

                req.getTargetContactName(),
                req.getTargetAccountName(),

                req.getPartnerRelationshipType(),
                req.getPartnerRelationshipDuration(),

                req.getWhyMeetingPreferred(),

                req.getMeetingDuration(),
                req.getMeetingAgenda(),

                req.getAvailableSlots()
        );
    }
}