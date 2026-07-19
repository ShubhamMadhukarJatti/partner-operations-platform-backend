package com.sharkdom.model.notification;

import com.sharkdom.entity.notification.Notification;

public class NotificationBuilder {

    Notification notification;

    public NotificationBuilder() {
        this.notification = new Notification();
    }

    public NotificationBuilder setSubject(String subject) {
        this.notification.setSubject(subject);
        return this;
    }

    public NotificationBuilder setBody(String body) {
        this.notification.setBody(body);
        return this;
    }

    public NotificationBuilder addRecordsInAdditionalDataMap(String newKeyValuesCombined) {
        if (this.notification.getAdditionalDataMap() == null || this.notification.getAdditionalDataMap().isEmpty()) {
            this.notification.setAdditionalDataMap(newKeyValuesCombined);
        } else {
            this.notification.setAdditionalDataMap(this.notification.getAdditionalDataMap() + ", " + newKeyValuesCombined);
        }
        return this;
    }

    public NotificationBuilder addIntoAdditionalDataMap(String key, String value) {
        return addRecordsInAdditionalDataMap(key + ":" + value);
    }

    public NotificationBuilder setSendEmail(boolean sendEmail) {
        this.notification.setSendEmail(sendEmail);
        return this;
    }

    public NotificationBuilder setUserId(String userId) {
        this.notification.setUserId(userId);
        return this;
    }

    public NotificationBuilder setForMobile(boolean forMobile) {
        this.notification.setForMobile(forMobile);
        return this;
    }

    public NotificationBuilder setForWeb(boolean forWeb) {
        this.notification.setForWeb(forWeb);
        return this;
    }

    public NotificationBuilder setMobileDeviceId(String mobileDeviceId) {
        this.notification.setMobileDeviceId(mobileDeviceId);
        return this;
    }

    public Notification build() {
        return this.notification;
    }


}
