package com.sharkdom.entity.notification;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.notification.NotificationFor;
import com.sharkdom.model.notification.NotificationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_unsubscribe")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NotificationUnsubscribe extends BaseEntity {
    private Long organizationId;
    private NotificationType type;
    private NotificationFor notificationFor;
}
