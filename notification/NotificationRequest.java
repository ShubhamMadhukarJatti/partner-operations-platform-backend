package com.sharkdom.model.notification;

public record NotificationRequest(Long organizationId, NotificationType type,
                                  NotificationFor notificationFor) {
}
