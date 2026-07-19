package com.sharkdom.model.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpgradeResponseDTO {

    private String status;
    private String url;
    private Double amount;
    private String currency;
    private Long upgradeId;
    private String error;

    public UpgradeResponseDTO(String status, String error) {
        this.status = status;
        this.error = error;
    }

}
