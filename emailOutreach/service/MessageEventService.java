package com.sharkdom.emailOutreach.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.emailOutreach.entity.MessageEvent;
import com.sharkdom.emailOutreach.repository.MessageEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class MessageEventService {

    @Autowired
    private MessageEventRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    public MessageEvent saveEvent(JsonNode payload) {
        JsonNode sig = payload.path("signature");
        JsonNode eventData = payload.path("event-data");

        String eventId = eventData.path("id").asText(null);
        long ts = eventData.path("timestamp").asLong(Instant.now().getEpochSecond());
        String eventType = eventData.path("event").asText("");
        String recipient = eventData.path("recipient").asText("");
        String domain = eventData.path("envelope").path("sender_domain").asText(
                eventData.path("message").path("headers").path("from").asText("")
        );
        String messageId = eventData.path("message").path("headers").path("message-id").asText(
                eventData.path("Message-Id").asText("")
        );
        JsonNode userVars = eventData.path("user-variables");
        String msgId = userVars.has("msg_id") ? userVars.path("msg_id").asText("") : "";
        // tags/campaign/url/severity etc
        String tagStr = "";
        if (eventData.has("tags")) {
            if (eventData.path("tags").isArray()) {
                StringBuilder sb = new StringBuilder();
                eventData.path("tags").forEach(t -> {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(t.asText());
                });
                tagStr = sb.toString();
            } else {
                tagStr = eventData.path("tag").asText("");
            }
        } else {
            tagStr = eventData.path("tag").asText("");
        }


        String urlClicked = "";
        if ("clicked".equalsIgnoreCase(eventType)) {
            urlClicked = eventData.path("url").asText(eventData.path("url-clicked").asText(""));
        }

        String severity = eventData.path("severity").asText(eventData.path("delivery-status").path("severity").asText(""));
        String reason = eventData.path("reason").asText("");
        String errMsg = eventData.path("delivery-status").path("message").asText("");
        String errCode = eventData.path("delivery-status").has("code") ? eventData.path("delivery-status").path("code").asText("") : "";

        String ip = eventData.path("client-info").path("client-ip").asText("");
        String ua = eventData.path("client-info").path("user-agent").asText("");
        String device = eventData.path("client-info").path("device-type").asText("");
        String country = eventData.path("geolocation").path("country").asText("");
        String city = eventData.path("geolocation").path("city").asText("");

        // create entity
        MessageEvent me = new MessageEvent();
        me.setEventIdentifier(eventId);
        me.setTs(ts);
        me.setDomain(domain);
        me.setEvent(eventType);
        me.setRecipient(recipient);
        me.setMessageId(messageId);
        me.setMsgId(msgId);
        me.setTag(tagStr);
        me.setUrl(urlClicked);
        me.setSeverity(severity);
        me.setReason(reason);
        me.setErrorMsg(errMsg);
        me.setCode(errCode);
        me.setCountry(country);
        me.setCity(city);
        me.setIp(ip);
        me.setDevice(device);
        me.setUa(ua);

        try {
            me.setRawJson(objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            me.setRawJson("{}");
        }

        // insert or ignore (we check by eventId)
        if (eventId != null && repository.findByEventIdentifier(eventId).isPresent()) {
            // already present: return existing or updated row if you want
            return repository.findByEventIdentifier(eventId).orElse(me);
        } else {
            return repository.save(me);
        }
    }

    /**
     * Do your per-event handling here — mirrors your PHP switch.
     */
    public void handleEvent(String eventType, JsonNode payload) {
        switch (eventType) {
            case "complained":
            case "unsubscribed":
                // add logic: e.g. mark user unsubscribed, push to queue, etc.
                break;
            case "failed":
                // handle failures
                break;
            case "clicked":
                // handle clicks
                break;
            case "opened":
            case "delivered":
            case "accepted":
            default:
                // default handling
                break;
        }
    }
}
