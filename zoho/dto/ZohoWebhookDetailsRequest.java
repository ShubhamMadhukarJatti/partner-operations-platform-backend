package com.sharkdom.zoho.dto;

public record ZohoWebhookDetailsRequest(

        Long organizationId,

        String apiDomain

) {
}
