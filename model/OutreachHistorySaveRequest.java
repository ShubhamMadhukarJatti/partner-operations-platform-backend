package com.sharkdom.agenticai.model;

import com.sharkdom.agenticai.enums.OutreachChannel;
import com.sharkdom.agenticai.enums.OutreachStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutreachHistorySaveRequest {

    private String companyName;

    private String recipientName;

    private String recipientTitle;

    private OutreachChannel channel;

    private OutreachStatus status;

    private String userId;

    private Long orgId;
}
