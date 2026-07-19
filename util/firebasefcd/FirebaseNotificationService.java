package com.sharkdom.util.firebasefcd;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.sharkdom.constants.NotificationSentStatus;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FirebaseNotificationService {

    @Autowired
    FirebaseMessaging firebaseMessaging;

    @Autowired
    NotificationService notificationService;

    private final static Queue<Notification> mobileNotifsQueue = new PriorityQueue<Notification>();

    @Async
    public static void addMessageInQueue(Notification notification) {
        if (notification.isForMobile()) {
            mobileNotifsQueue.add(notification);
        }
    }

    public static void addMessagesInQueue(List<Notification> notifications) {
        notifications.forEach(FirebaseNotificationService::addMessageInQueue);
    }

    @Scheduled(fixedRateString = "${notification.mobile.frequency_millis}")
    public void consumeQueueAndSendNotifs() {
        log.info("Triggered consumeQueueAndSendNotifs!");

        List<Notification> updatedNotifications = new ArrayList<>();
        while (!mobileNotifsQueue.isEmpty()) {
            log.info("started consuming mobileNotifsQueue, total messages remaining: " + mobileNotifsQueue.size());
            Notification notification = mobileNotifsQueue.remove();
            if (sendMobileNotification(buildFirebaseMessage(notification))) {
                notification.setMobileSentStatus(NotificationSentStatus.SUCCESS);
            } else {
                notification.setMobileSentStatus(NotificationSentStatus.FAILED);
            }
            updatedNotifications.add(notification);
        }
        try {
            notificationService.saveAll(updatedNotifications);
        } catch (Exception e) {
            log.error("Failed to update status of notifications!");
            e.printStackTrace();
        }
    }

    private Message buildFirebaseMessage(Notification firebaseNotification) {
        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification
                .builder()
                .setTitle(firebaseNotification.getSubject())
                .setBody(firebaseNotification.getBody())
                .build();

        Map<String, String> additionalDataMap = new HashMap<>();
        try {
            String[] valuesArray = firebaseNotification.getAdditionalDataMap().split(",");
            additionalDataMap = Arrays.stream(valuesArray).collect(
                    Collectors.toMap(
                            keyValuePair -> keyValuePair.split(":")[0].trim(),
                            keyValuePair -> keyValuePair.split(":")[1].trim()));
        } catch (Exception e) {
            log.error("Unable to extract key value pair from AdditionalDataMap for notification with id " + firebaseNotification.getId());
        }
        return Message
                .builder()
                .setToken(firebaseNotification.getMobileDeviceId())
                .setNotification(notification)
                .putAllData(additionalDataMap)
                .build();
    }

    private boolean sendMobileNotification(Message message) {
        try {
            firebaseMessaging.send(message);
            log.info("FCD sent message successfully!");
            return true;
        } catch (Exception e) {
            log.error("FCD failed to send message!");
            e.printStackTrace();
        }
        return false;
    }
}
