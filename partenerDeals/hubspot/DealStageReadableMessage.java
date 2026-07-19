package com.sharkdom.service.partenerDeals.hubspot;

public enum DealStageReadableMessage {

    QUALIFIED_TO_BUY("qualifiedtobuy", "You qualified the lead", "Vendor qualified the lead"),
    PRESENTATION_SCHEDULED("presentationscheduled", "You scheduled a presentation", "Vendor scheduled a presentation"),
    DECISION_MAKER_BOUGHT_IN("decisionmakerboughtin", "You got the decision maker's buy-in", "Vendor got the decision maker's buy-in"),
    CONTRACT_SENT("contractsent", "You sent the contract", "Vendor sent the contract"),
    CLOSED_WON("closedwon", "You closed the deal", "Vendor closed the deal"),
    CLOSED_LOST("closedlost", "You lost the deal", "Vendor lost the deal"),
    APPOINTMENT_SCHEDULED("appointmentscheduled", "You scheduled the appointment", "Vendor scheduled the appointment");

    private final String key;
    private final String buyerMessage;
    private final String vendorMessage;

    DealStageReadableMessage(String key, String buyerMessage, String vendorMessage) {
        this.key = key;
        this.buyerMessage = buyerMessage;
        this.vendorMessage = vendorMessage;
    }

    public static String getReadableMessage(String value, boolean isVendor) {
        for (DealStageReadableMessage stage : values()) {
            if (stage.key.equalsIgnoreCase(value)) {
                return isVendor ? stage.buyerMessage : stage.vendorMessage;
            }
        }
        return value; // fallback to raw value
    }
}

