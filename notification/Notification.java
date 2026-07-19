package com.sharkdom.entity.notification;

import com.sharkdom.constants.NotificationSentStatus;
import com.sharkdom.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "notification", indexes = {@Index(columnList = "userId", name = "notification_userId")})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Notification extends BaseEntity implements Comparable {

    private static final long serialVersionUID = 1L;
    private String subject;
    private String body;
    private boolean isRead = false;
    @Schema(name = "additionalDataMap", example = "clickAction:OpenActivity, campaign:discountFifty", description = "Multiple key values separated by comma")
    private String additionalDataMap;
    private boolean sendEmail = false;
    private String userId;
    private boolean forMobile = false;
    private boolean forWeb = false;
    private String mobileDeviceId;
    private Long organizationId;
    private NotificationSentStatus mobileSentStatus = NotificationSentStatus.PENDING;
    private String templateCode;
    private List<String> preferredSectors;
    private List<String> preferredPartnershipTypes;
    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
