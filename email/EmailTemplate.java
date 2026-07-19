package com.sharkdom.model.email;

import lombok.Data;

@Data
public class EmailTemplate {

    private String templateDescription;
    private String bodyHtml;
    private String bodyString;
    private String sender;
    private String subject;
    private String s3AttachmentNames;

}
