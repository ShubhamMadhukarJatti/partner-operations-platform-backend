package com.sharkdom.repository.notification;

import com.sharkdom.entity.notification.NotificationUnsubscribe;
import com.sharkdom.model.notification.NotificationFor;
import com.sharkdom.model.notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface NotificationUnsubscribeRepository extends JpaRepository<NotificationUnsubscribe, Long> {
    Optional<NotificationUnsubscribe> findByOrganizationIdAndTypeAndNotificationFor(Long organizationId, NotificationType type, NotificationFor notificationFor);
}

