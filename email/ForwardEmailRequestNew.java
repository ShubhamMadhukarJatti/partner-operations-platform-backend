package com.sharkdom.model.email;

import lombok.Data;

@Data
public class ForwardEmailRequestNew {
    private String userId;
    private String toEmail;
    private String replyBodyHtml;

}
