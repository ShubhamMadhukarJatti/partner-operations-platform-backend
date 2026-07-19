package com.sharkdom.entity.ai;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "persona_user_notify")
@Data
public class PersonaUserNotifyEntity extends BaseEntity {
    @Column(name = "sender_org_id", nullable = false)
    private Long senderOrgId;

    @Column(name = "reciever_org_id", nullable = false)
    private Long recieverOrgId;

    @Column(name = "is_notified")
    private Boolean isNotified;
}
