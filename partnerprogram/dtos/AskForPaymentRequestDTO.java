package com.sharkdom.partnerprogram.dtos;

import lombok.Data;

@Data
public class AskForPaymentRequestDTO {

    private String userId;
    private String reason;
    private String notes;
}
