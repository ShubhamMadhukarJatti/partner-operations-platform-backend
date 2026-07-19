package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailReqModelWithMultipartAttachments extends EmailReqModel {
    private List<MultipartFile> attachmentList;

    public EmailReqModelWithMultipartAttachments(EmailReqModel emailReqModel) {
        super(emailReqModel.getRecipients(), emailReqModel.getBodyHtml(), emailReqModel.getBodyText(), emailReqModel.getSubject(),
                emailReqModel.getS3AttachmentNames(), emailReqModel.getFrom());
    }

    public EmailReqModelWithMultipartAttachments(String sender, List<String> recipients, String bodyHtml, String bodyText, String subject, List<String> s3AttachmentNames, List<MultipartFile> attachmentList, String from) {
        super(recipients, bodyHtml, bodyText, subject, s3AttachmentNames, from);
        this.attachmentList = attachmentList;
    }

    public EmailReqModelWithMultipartAttachments(EmailReqModel emailReqModel, List<MultipartFile> attachmentList) {
        super(emailReqModel.getRecipients(), emailReqModel.getBodyHtml(), emailReqModel.getBodyText(), emailReqModel.getSubject(),
                emailReqModel.getS3AttachmentNames(), emailReqModel.getFrom());
        this.attachmentList = attachmentList;
    }
}