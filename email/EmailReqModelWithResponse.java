package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailReqModelWithResponse {

    private String sender;
    private List<String> recipients;
    private String bodyHtml;
    private String bodyText;
    private String subject;
    private List<String> s3AttachmentNames;
    private ResponseEntity<String> response;

    public EmailReqModelWithResponse(EmailReqModel emailReqModelWithMultipartAttachments) {
        this.recipients = emailReqModelWithMultipartAttachments.getRecipients();
        this.bodyHtml = emailReqModelWithMultipartAttachments.getBodyHtml();
        this.bodyText = emailReqModelWithMultipartAttachments.getBodyText();
        this.subject = emailReqModelWithMultipartAttachments.getSubject();
        this.s3AttachmentNames = emailReqModelWithMultipartAttachments.getS3AttachmentNames();
    }

    public EmailReqModelWithResponse(EmailReqModel emailReqModelWithMultipartAttachments, ResponseEntity<String> response) {
        this(emailReqModelWithMultipartAttachments);
        this.response = response;
    }

}
