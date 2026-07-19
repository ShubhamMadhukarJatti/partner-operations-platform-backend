package com.sharkdom.service.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.config.AppProperties;
import com.sharkdom.constants.subscription.SubscriptionEmailEvent;
import com.sharkdom.entity.subscription.Subscription;
import com.sharkdom.model.email.TemplateEmailReqModel;
import com.sharkdom.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubscriptionEmailService {

    @Autowired
    EmailService emailService;
    @Autowired
    AppProperties appProperties;

    @Async
    public void sendNotifyEmail(SubscriptionEmailEvent event, Subscription subscription) {
        try {
            log.info("inside sendNotifyEmail");
            String templateCode = appProperties.getEmailTemplateCodeForEvent(event.toString());
            TemplateEmailReqModel templateEmailReqModel = new TemplateEmailReqModel(templateCode, List.of(subscription.getUserId()), null, null, null, null, null, null, null, null, null, null, null, null, null, null);

            Map<String, Map<String, Object>> subscriptionDataMap = getSubscriptionDataMapForTemplate(subscription);
            log.info("inside sendNotifyEmail, calling sendByTemplateAndUserIds for template " + templateCode);
            emailService.sendByTemplateAndUserIds(templateEmailReqModel, subscriptionDataMap);
        } catch (Exception e) {
            log.error("Failed to sent email for subscription event " + event.toString() + " of user with userId " + subscription.getUserId() == null ? " " : subscription.getUserId());
            //TODO add log in logger table??
        }
    }

    /* to prepare Map of userId and subscription table data
    key of Map will be userId of the user and value will be subscription table columns and their values
    Example:
            "userIdXYZ" : [ {"subscription.planCode" : "standard" , "subscription.endOn" : "20-06-2023"}]
     */
    public Map<String, Map<String, Object>> getSubscriptionDataMapForTemplate(Subscription subscription) {

        Map<String, Object> susbcriptionDataMap = new ObjectMapper().convertValue(subscription, Map.class);

        //append subscription at the beginning of all keys
        susbcriptionDataMap = susbcriptionDataMap.entrySet().stream().map(entry -> {
            return new HashMap.SimpleEntry<>("subscription." + entry.getKey(), (entry.getValue() == null) ? "" : entry.getValue());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Map.of(subscription.getUserId(), susbcriptionDataMap);
    }
}
