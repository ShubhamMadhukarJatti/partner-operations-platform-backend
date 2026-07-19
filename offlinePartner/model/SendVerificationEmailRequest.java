package com.sharkdom.offlinePartner.model;

public record SendVerificationEmailRequest(String email,
                                           String subject,
                                           String body,
                                           Long organizationId) {
}
