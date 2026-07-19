package com.sharkdom.emailOutreach.service;

import com.sharkdom.emailOutreach.dto.SendMailRequest;
import com.sharkdom.emailOutreach.dto.SendMailResponse;
import com.sharkdom.emailOutreach.entity.Email;
import com.sharkdom.emailOutreach.entity.MailgunEmail;
import com.sharkdom.emailOutreach.repository.EmailRepository;
import com.sharkdom.emailOutreach.repository.MailgunEmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MailgunServices {

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.api.key}")
    private String apiKey;

    @Value("${mailgun.signing-key}")
    private String signingKey;

    @Value("${mailgun.region}")
    private String region;

    private static final String MAILGUN_API_BASE = "https://api.mailgun.net/v3/";

    @Autowired
    private MailgunEmailRepository repo;

    @Autowired
    private EmailRepository emailRepository;



    private RestTemplate rest = new RestTemplate();
    private static Pattern REPLY_PATTERN = Pattern.compile("^reply\\+([^@]+)@(.+)$", Pattern.CASE_INSENSITIVE);


    public SendMailResponse sendEmail(SendMailRequest request) {
        String url = MAILGUN_API_BASE + domain + "/messages";

        // Authorization
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Generate unique threadId (UUID or DB-generated value)
        String threadId = UUID.randomUUID().toString().replace("-", "").substring(0, 24);

        // Build request body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("from", request.getFrom()); // fixed sender
        body.add("to", request.getTo());
        body.add("subject", request.getSubject());
        body.add("html", request.getBody());

        // Add reply-to with unique threadId
        String replyTo = "reply+" + threadId + "@mg.sharkdom.com";
        body.add("h:Reply-To", replyTo);

        // Also include optional message/thread identifiers (helps for tracking)
        body.add("h:Message-Id", "<msg_" + threadId + "@sharkdom.com>");
        body.add("v:thread_id", threadId);
        body.add("o:tag", "thread:" + threadId);

        // optional tracking
        body.add("o:tracking", "yes");
        body.add("o:tracking-opens", "yes");
        body.add("o:tracking-clicks", "yes");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<SendMailResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, SendMailResponse.class);

            log.info("Mailgun response: {}", response.getBody());
            SendMailResponse bo = response.getBody();
            if (bo != null) {
                bo.setThreadId(threadId);// set threadId in response
                log.info("Assigned threadId {} to email send response", threadId);
            }
            return bo;
        } catch (Exception e) {
            log.error("Error sending email with Mailgun", e);
            SendMailResponse errorResponse = new SendMailResponse();
            errorResponse.setId("error");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public boolean verifySignature(String ts, String token, String sig) {
        try {
            if (ts == null || token == null || sig == null) return false;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String digest = Hex.encodeHexString(mac.doFinal((ts + token).getBytes(StandardCharsets.UTF_8)));
            return slowEquals(digest, sig);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean slowEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }

    public MailgunEmail saveInbound(Map<String, String> form, String ip) {
        log.info("Inbound email data: {}", form);
        MailgunEmail mail = new MailgunEmail();

        String recipient = form.getOrDefault("recipient", form.getOrDefault("To", ""));
        Matcher m = REPLY_PATTERN.matcher(recipient);
        if (m.matches()) mail.setThreadToken(m.group(1));

        mail.setFromRaw(form.getOrDefault("sender", form.getOrDefault("from", "")));
        mail.setFromEmail(parseEmailOnly(mail.getFromRaw()));
        mail.setToRaw(recipient);
        mail.setSubject(form.getOrDefault("subject", ""));
        mail.setTextBody(form.getOrDefault("stripped-text", form.getOrDefault("body-plain", "")));
        mail.setHtmlBody(form.getOrDefault("stripped-html", form.getOrDefault("body-html", "")));
        mail.setMessageId(form.getOrDefault("Message-Id", form.getOrDefault("message-id", "")));
        mail.setInReplyTo(form.getOrDefault("In-Reply-To", form.getOrDefault("in-reply-to", "")));
        mail.setReferencesField(form.getOrDefault("References", form.getOrDefault("references", "")));
        mail.setIp(ip);

        MailgunEmail save = repo.save(mail);

        if (save!= null) {
            Optional<Email> optEmail = emailRepository.findByThreadId(mail.getThreadToken());
            log.info("Found linked email for thread {}: {}", mail.getThreadToken(), optEmail.isPresent());
            if (optEmail.isPresent()) {
                log.info("Auto-replying to email from {} regarding thread {}", mail.getFromEmail(), mail.getThreadToken());
                SendMailRequest request = new SendMailRequest();
                MailgunEmail lastEmail = getLastEmailByThreadToken(mail.getThreadToken());
                if (lastEmail != null) {
                    log.info("Last email found for thread {} -> Subject: {}", lastEmail.getThreadToken(), lastEmail.getSubject());
                    if (lastEmail.getFromEmail().equals(optEmail.get().getSenderOrgEmail()))
                    {
                        request.setFrom(optEmail.get().getSenderOrgEmail());
                        request.setTo(optEmail.get().getTo());
                        log.info("Preparing auto-reply: From 1: {}, To: {}", request.getFrom(), request.getTo());
                    }
                    else if (lastEmail.getFromEmail().equals(optEmail.get().getTo())){
                        request.setFrom(lastEmail.getFromEmail());
                        request.setTo(optEmail.get().getSenderOrgEmail());
                        log.info("Preparing auto-reply: From 2: {}, To: {}", request.getFrom(), request.getTo());
                    }
                    else
                    {
                        request.setFrom(optEmail.get().getTo());
                        request.setTo(optEmail.get().getSenderOrgEmail());
                        log.info("Preparing auto-reply: From 3: {}, To: {}", request.getFrom(), request.getTo());
                    }
                } else {
                    request.setFrom(optEmail.get().getTo());
                    request.setTo(optEmail.get().getSenderOrgEmail());
                    log.info("No email found for thread {}", mail.getThreadToken());
                    log.info("Preparing auto-reply: From 4: {}, To: {}", request.getFrom(), request.getTo());
                }
                log.info("Preparing auto-reply: From: {}, To: {}", request.getFrom(), request.getTo());
                request.setSubject("Re: " + mail.getSubject());
                request.setBody(mail.getTextBody() != null && !mail.getTextBody().isEmpty()
                        ? mail.getTextBody() : mail.getHtmlBody());
                SendMailResponse sendMailResponse = forwardMail(request, mail.getThreadToken());
                if (sendMailResponse != null) {
                    log.info("Auto-reply sent successfully: {}", sendMailResponse);
                } else {
                    log.error("Failed to send auto-reply for thread {}", mail.getThreadToken());
                }
            }
        }
        return save;
    }

    public MailgunEmail getLastEmailByThreadToken(String threadToken) {
        return repo.findTopByThreadTokenOrderByCreationTimestampDesc(threadToken)
                .orElse(null);
    }

    private String parseEmailOnly(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("<([^>]+)>").matcher(s);
        if (m.find()) return m.group(1).trim().toLowerCase();
        return s.trim().toLowerCase();
    }

    public SendMailResponse forwardMail(SendMailRequest request,String threadId) {
        String url = MAILGUN_API_BASE + domain + "/messages";

        // Authorization
        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Build request body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("from", request.getFrom());
        body.add("to", request.getTo());
        body.add("subject", request.getSubject());
        body.add("html", request.getBody());

        // Add reply-to with unique threadId
        String replyTo = "reply+" + threadId + "@mg.sharkdom.com";
        body.add("h:Reply-To", replyTo);

        // Also include optional message/thread identifiers (helps for tracking)
        body.add("h:Message-Id", "<msg_" + threadId + "@sharkdom.com>");
        body.add("v:thread_id", threadId);
        body.add("o:tag", "thread:" + threadId);

        // optional tracking
        body.add("o:tracking", "yes");
        body.add("o:tracking-opens", "yes");
        body.add("o:tracking-clicks", "yes");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<SendMailResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, SendMailResponse.class);

            log.info("Mailgun response: {}", response.getBody());
            SendMailResponse bo = response.getBody();
            if (bo != null) {
                bo.setThreadId(threadId); // set threadId in response
            }
            return bo;
        } catch (Exception e) {
            log.error("Error sending email with Mailgun", e);
            SendMailResponse errorResponse = new SendMailResponse();
            errorResponse.setId("error");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }
}

