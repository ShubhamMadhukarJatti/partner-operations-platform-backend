package com.sharkdom.service.email;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.sharkdom.util.aws.config.AwsConfigsProvider;
import com.sharkdom.util.aws.service.AmazonS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class AmazonSes {
    @Value("${env}")
    private String env;

    @Autowired
    private AmazonS3Service amazonS3Service;

    private static void sendUsingSes(String recipients, MimeMessage message) throws Exception {
        try {
            log.info("Attempting to send an email through Amazon SES to recipient: " + recipients);

            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(AwsConfigsProvider.getSesConfigs().get("accessKey"),
                                    AwsConfigsProvider.getSesConfigs().get("secretKey"))))
                    .withRegion(Regions.AP_SOUTH_1).build();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
            client.sendRawEmail(rawEmailRequest);
            log.info("Email sent to recipient: " + recipients);
        } catch (Exception ex) {
            log.error("Failed to send email to recipient: " + recipients);
            throw ex;
        }
    }

    private static MimeMultipart attachMultipartFiles(List<MultipartFile> attachmentList, MimeMultipart msg) throws IOException, MessagingException {
        if (null != attachmentList && !attachmentList.isEmpty()) {
            for (MultipartFile attachment : attachmentList) {
                MimeBodyPart att = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(attachment.getBytes(), "application/pdf");
                att.setDataHandler(new DataHandler(source));
                att.setFileName(attachment.getOriginalFilename());
                msg.addBodyPart(att);
            }
        }
        return msg;
    }

    public void prepareAndSend(String subject, String bodyHtml, List<String> s3AttachmentNames, String from, String recipients, List<MultipartFile> attachmentList, String templateCode, Long collaborationId)
            throws Exception {

        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));

        message.setSubject(subject, "UTF-8");
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        message.setHeader("templateCode", templateCode);
        message.setHeader("env", env);
        message.setHeader("sentAt", LocalDate.now().toString());
        message.setHeader("collaborationId", collaborationId != null ? collaborationId.toString() : "");


        MimeMultipart msg_body = new MimeMultipart("alternative");

        MimeBodyPart wrap = new MimeBodyPart();
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(bodyHtml, "text/html; charset=UTF-8");

        msg_body.addBodyPart(htmlPart);
        wrap.setContent(msg_body);

        MimeMultipart msg = new MimeMultipart("mixed");
        msg.addBodyPart(wrap);
        msg = attachFilesFromS3(s3AttachmentNames, msg);
        msg = attachMultipartFiles(attachmentList, msg);
        message.setContent(msg);
        sendUsingSes(recipients, message);
    }

    private MimeMultipart attachFilesFromS3(List<String> s3AttachmentNames, MimeMultipart msg) throws IOException, ExecutionException, InterruptedException, MessagingException {
        if (null != s3AttachmentNames && !s3AttachmentNames.isEmpty()) {
            for (String attachmentName : s3AttachmentNames) {
                MimeBodyPart att = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(amazonS3Service.downloadFileFromS3(attachmentName),
                        "application/pdf");
                att.setDataHandler(new DataHandler(source));
                att.setFileName(attachmentName);
                msg.addBodyPart(att);
            }
        }
        return msg;
    }

    public void prepareAndSendInvoice(String subject, String bodyHtml, String invoiceName, String from, String recipients, InputStream s3Attach, String templateCode, Long collaborationId) throws Exception {

        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));

        message.setSubject(subject, "UTF-8");
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        message.setHeader("templateCode", templateCode);
        message.setHeader("env", env);
        message.setHeader("sentAt", LocalDate.now().toString());
        message.setHeader("collaborationId", collaborationId.toString());

        MimeMultipart msgBody = new MimeMultipart("alternative");

        MimeBodyPart wrap = new MimeBodyPart();
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(bodyHtml, "text/html; charset=UTF-8");

        msgBody.addBodyPart(htmlPart);
        wrap.setContent(msgBody);

        MimeMultipart msg = new MimeMultipart("mixed");
        msg.addBodyPart(wrap);
        attachInvoiceFromS3(s3Attach, msg, invoiceName);
        message.setContent(msg);
        sendUsingSes(recipients, message);
    }

    private static void attachInvoiceFromS3(InputStream s3Attach, MimeMultipart msg, String invoiceName) throws IOException, MessagingException {
        if (!ObjectUtils.isEmpty(s3Attach)) {
            MimeBodyPart att = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(s3Attach, "application/pdf");
            att.setDataHandler(new DataHandler(source));
            att.setFileName(invoiceName);
            msg.addBodyPart(att);
        }
    }
}
