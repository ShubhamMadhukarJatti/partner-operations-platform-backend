package com.sharkdom.agenticai.entity;

import com.sharkdom.agenticai.enums.OutreachChannel;
import com.sharkdom.agenticai.enums.OutreachStatus;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "outreach_history")
public class OutreachHistory extends BaseEntity {

    private String companyName;

    private String recipientName;

    private String recipientTitle;

    @Enumerated(EnumType.STRING)
    private OutreachChannel channel;

    @Enumerated(EnumType.STRING)
    private OutreachStatus status;

    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String userId;

    private Long orgId;

}

