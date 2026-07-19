package com.sharkdom.emailOutreach.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.emailOutreach.entity.MailgunEmail;
import com.sharkdom.emailOutreach.repository.MailgunEmailRepository;
import com.sharkdom.emailOutreach.service.MailgunServices;
import com.sharkdom.emailOutreach.service.MessageEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

@Slf4j
@RestController
@RequestMapping("/mailgun")
public class MailgunWebhookController {

    private final MessageEventService webhookService;
    private final ObjectMapper objectMapper;
    private final MailgunServices mailgunServices;

    @Autowired
    private MailgunEmailRepository mailgunEmailRepository;

    @Value("${mailgun.signing-key}")
    private String signingKey;

    public MailgunWebhookController(MessageEventService webhookService,
                                    ObjectMapper objectMapper, MailgunServices mailgunServices) {
        this.webhookService = webhookService;
        this.objectMapper = objectMapper;
        this.mailgunServices= mailgunServices;
    }

    @PostMapping(
            value = "/webhook",
            consumes = {
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE
            }
    )
    public ResponseEntity<?> handleWebhook(HttpServletRequest request) throws Exception {
        log.info("Received Mailgun webhook: Content-Type={}", request.getContentType());

        // --- Read raw body ---
        byte[] rawBytes = StreamUtils.copyToByteArray(request.getInputStream());
        String raw = new String(rawBytes, StandardCharsets.UTF_8);

        // Log raw only in debug mode (avoid flooding prod logs)
        if (log.isDebugEnabled()) {
            log.debug("Raw webhook body: {}", raw);
        }

        // --- Parse payload ---
        JsonNode payload = parsePayload(request.getContentType(), raw);
        if (payload == null || !payload.has("signature") || !payload.has("event-data")) {
            log.warn("Invalid payload parsed. raw length={} ct={}", raw.length(), request.getContentType());
            return ResponseEntity.badRequest().body(Map.of("status", "invalid-payload"));
        }

        // Pretty-print JSON payload in debug mode
        if (log.isDebugEnabled()) {
            log.debug("Parsed payload:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
        }

        // --- Signature validation ---
        if (signingKey == null || signingKey.isBlank()) {
            return ResponseEntity.status(500).body(Map.of("status", "server-misconfigured"));
        }
        JsonNode sig = payload.get("signature");
        String timestamp = sig.path("timestamp").asText("");
        String token = sig.path("token").asText("");
        String signature = sig.path("signature").asText("");

        if (!verifySignature(signingKey, timestamp, token, signature)) {
            log.warn("Signature verification failed for timestamp={} token={}", timestamp, token);
            return ResponseEntity.status(403).body(Map.of("status", "bad-signature"));
        }

        // --- Process event ---
        JsonNode eventData = payload.get("event-data");
        String eventType = eventData.path("event").asText("");
        String eventId = eventData.path("id").asText("");
        String recipient = eventData.path("recipient").asText("");

        try {
            webhookService.saveEvent(payload);
            webhookService.handleEvent(eventType, payload);
            log.info("Processed event={} id={} recipient={}", eventType, eventId, recipient);
        } catch (Exception ex) {
            log.error("Error processing event={} id={}", eventType, eventId, ex);
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "event", eventType,
                "id", eventId
        ));
    }

    private JsonNode parsePayload(String contentType, String raw) {
        log.info("Parsing payload with Content-Type: {} rawLen={}", contentType, raw == null ? 0 : raw.length());
        try {
            if (contentType != null) {
                contentType = contentType.toLowerCase();

                // --- JSON case ---
                if (contentType.contains("application/json")) {
                    return objectMapper.readTree(raw);
                }

                // --- x-www-form-urlencoded or multipart/form-data ---
                if (contentType.contains("application/x-www-form-urlencoded") ||
                        contentType.contains("multipart/form-data")) {

                    Map<String, String> form = new HashMap<>();
                    if (raw != null && !raw.isBlank()) {
                        StringTokenizer st = new StringTokenizer(raw, "&");
                        while (st.hasMoreTokens()) {
                            String pair = st.nextToken();
                            int idx = pair.indexOf('=');
                            if (idx >= 0) {
                                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                                form.put(k, v);
                            }
                        }
                    }

                    log.debug("Form fields received: {}", form);

                    // Wrap into Mailgun-like JSON
                    Map<String, Object> wrapper = new HashMap<>();
                    wrapper.put("signature", Map.of(
                            "timestamp", form.getOrDefault("timestamp", ""),
                            "token", form.getOrDefault("token", ""),
                            "signature", form.getOrDefault("signature", "")
                    ));

                    String eventDataStr = form.get("event-data");
                    if (eventDataStr != null && !eventDataStr.isBlank()) {
                        try {
                            wrapper.put("event-data", objectMapper.readTree(eventDataStr));
                        } catch (Exception ex) {
                            wrapper.put("event-data", form); // fallback
                        }
                    } else {
                        wrapper.put("event-data", form);
                    }

                    return objectMapper.valueToTree(wrapper);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error parsing payload", e);
            return null;
        }
    }

    private boolean verifySignature(String key, String timestamp, String token, String signature) {
        if (timestamp == null || timestamp.isBlank()) return false;
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException nfe) {
            return false;
        }

        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - ts) > 300) return false; // 5 min tolerance

        try {
            String data = timestamp + token;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expected = bytesToHex(digest);
            return constantTimeEquals(expected, signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    @PostMapping(value = "/data", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity<String> inbound(@RequestParam Map<String, String> form,
                                          @RequestParam(required = false) MultiValueMap<String, MultipartFile> files,
                                          HttpServletRequest req) {
        log.info("mailgun Inbound called. remoteAddr={}", req.getRemoteAddr());


        log.debug("Form fields: {}", form.keySet());
        if (files != null) {
            log.debug("Files present: {}", files.keySet());
        }


        String ts = form.get("timestamp");
        String token = form.get("token");
        String sig = form.get("signature");
        if (ts == null || token == null || sig == null) {
            log.warn("Signature missing in form fields");

            return ResponseEntity.status(400).body("missing-signature");
        }
        if (!mailgunServices.verifySignature(ts, token, sig)) {
            log.warn("Invalid signature for inbound mail");
            return ResponseEntity.status(403).body("invalid signature");
        }


        if (files != null && !files.isEmpty()) {
            files.forEach((name, multipartFiles) -> {
                for (MultipartFile mf : multipartFiles) {
                    try {
                        String filename = mf.getOriginalFilename() == null ? "unknown" : mf.getOriginalFilename();
                        java.nio.file.Path out = java.nio.file.Paths.get("/tmp/mailgun_attach_" + System.currentTimeMillis() + "_" + filename);
                        java.nio.file.Files.copy(mf.getInputStream(), out);
                        log.info("Saved attachment {} to {}", name, out);
                    } catch (Exception e) {
                        log.error("Failed to save attachment {}", mf.getOriginalFilename(), e);
                    }
                }
            });
        }


        MailgunEmail mail = mailgunServices.saveInbound(form, req.getRemoteAddr());
        log.info("Saved inbound mail id={}", mail != null ? mail.getId() : "null");

        return ResponseEntity.ok("ok");
    }

    private boolean verifySignatur(String key, String timestamp, String token, String signature) {
        if (timestamp == null || timestamp.isBlank()) return false;
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException nfe) {
            return false;
        }

        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - ts) > 300) return false; // 5 min tolerance

        try {
            String data = timestamp + token;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expected = bytesToHe(digest);
            return constantTimeEqual(expected, signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }

    private boolean constantTimeEqual(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }

    private static String bytesToHe(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

}
