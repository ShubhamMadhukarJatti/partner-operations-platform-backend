package com.sharkdom.model.subscription;

import java.time.LocalDate;

public interface SubscriptionExpiring {
    Long getOrganizationId();

    LocalDate getEndOn();

    String getPlanCode();
}
