package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailReqModel {
    private List<String> recipients;
    private String bodyHtml;
    private String bodyText;
    private String subject;
    private List<String> s3AttachmentNames;
    private String from;

    public EmailReqModelWithMultipartAttachments withMultipartAttachments(List<MultipartFile> attachmentList) {
        return new EmailReqModelWithMultipartAttachments(this, attachmentList);
    }
}