package com.sharkdom.constants.partnerDeals;

public enum DealStage {

    // General stages
    APPROVED,
    WAITING_FOR_APPROVAL,
    REJECTED,
    CLOSED,
    EXPIRED,

    // HubSpot stages
    APPOINTMENT_SCHEDULED,
    QUALIFIED_TO_BUY,
    PRESENTATION_SCHEDULED,
    DECISION_MAKER_BOUGHT_IN,
    CONTRACT_SENT,

    // Salesforce stages
    QUALIFICATION,
    NEEDS_ANALYSIS,
    PROPOSAL,
    NEGOTIATION,
    CLOSED_WON,
    CLOSED_LOST,

    // Zoho stages
    VALUE_PROPOSITION,
    IDENTIFY_DECISION_MAKERS,
    PROPOSAL_PRICE_QUOTE,
    NEGOTIATION_REVIEW,

    DEMO,
    DISCOVERY
}
