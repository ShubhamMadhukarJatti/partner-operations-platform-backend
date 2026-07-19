package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.VendorPartnerEmailRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class VendorPartnerEmailService {

    public String generateVendorPartnerPrompt(VendorPartnerEmailRequestDTO req) {

        return """
                You are %s, %s at %s.
                
                Write a concise, genuine email to %s, %s at %s requesting that they make an email introduction on your behalf to %s, %s at %s.
                
                Your current stage with this account is %s.
                The partner's relationship with the account is %s.
                How long partner has known this account is %s.
                
                Why your product is relevant right now: %s.
                
                What you are asking the partner to do:
                Send a 3-way email introducing %s to %s.
                
                You will draft the intro email for them so they only need to forward or send it.
                
                Any shared context between you and the partner: %s.
                
                Follow the rules:
                - Maximum 120 words
                - Open with the account name immediately
                - Acknowledge their relationship explicitly
                - Make the ask small (one email)
                - Offer the pre-drafted email explicitly
                - No pressure language
                - End with yes/no question
                - Tone warm and slightly informal
                """.formatted(
                req.getSenderName(),
                req.getSenderTitle(),
                req.getSenderCompany(),

                req.getPartnerContactName(),
                req.getPartnerContactTitle(),
                req.getPartnerCompany(),

                req.getTargetContactName(),
                req.getTargetContactTitle(),
                req.getTargetAccountName(),

                req.getYourDealStage(),
                req.getPartnerRelationshipType(),
                req.getPartnerRelationshipDuration(),

                req.getRelevanceReason(),
                req.getSenderName(),
                req.getTargetContactName(),

                req.getPriorContext()
        );
    }
}