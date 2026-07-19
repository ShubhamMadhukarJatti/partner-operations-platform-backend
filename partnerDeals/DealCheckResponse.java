package com.sharkdom.model.partnerDeals;

import lombok.Data;

@Data
public class DealCheckResponse {
    private String message;
    private boolean alreadyExistsWithSameVendor = false;
    private boolean alreadyExistsWithAnotherVendor = false;
}
