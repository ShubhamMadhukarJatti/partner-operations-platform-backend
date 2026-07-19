package com.sharkdom.agenticai.model;

import com.sharkdom.agenticai.enums.OutreachChannel;
import com.sharkdom.agenticai.enums.OutreachStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OutreachHistoryResponse {

    private Long id;

    private String companyName;

    private String recipientName;

    private String recipientTitle;

    private OutreachChannel channel;

    private OutreachStatus status;

    private String userId;

    private Long orgId;

    private LocalDateTime sentAt;
}