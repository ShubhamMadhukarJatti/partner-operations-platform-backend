package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailForwardReqModelWithResponse {

    private String uuid;     // unique id for the email transaction
    private String status;   // SUCCESS / FAILED
    private String message;
    private String body;   // <-- added field


    public EmailForwardReqModelWithResponse(String uuid, String status, String messageBody) {
    }
}
