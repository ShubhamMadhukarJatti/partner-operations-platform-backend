package com.sharkdom.model.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpgradeConfirmationDTO {

    private String status;
    private String subscriptionId;
    private String newPriceId;
    private long currentPeriodEnd;

    public UpgradeConfirmationDTO(String status, String error) {
        this.status = status;
        this.subscriptionId = error;
    }

}
