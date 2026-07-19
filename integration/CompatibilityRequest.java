package com.sharkdom.model.integration;

import java.util.List;

public record CompatibilityRequest(String email, String organizationName, String startupInfo, List<String> sectors,
                                   String partnershipGoal) {
}
