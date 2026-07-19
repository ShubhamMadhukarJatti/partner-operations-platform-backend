package com.sharkdom.model.organizatiocollaboration;

import com.sharkdom.constants.Flag;
import com.sharkdom.constants.LinkerType;
import com.sharkdom.entity.organizationcollaboration.ChannelFlag;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class OrganizationMessagesResponse {
    private Long id;
    private Date creationTimestamp;
    private Date lastUpdatedTimestamp;
    private Long chatRoomId;
    private String query;
    private Long linkerId;
    private LinkerType linkerType;
    private Flag flag;
    @Enumerated(EnumType.STRING)
    private ChannelFlag channelFlag;
    private boolean isRead;
    private LocalDateTime readAt;
    private Long senderId;
    private Long receiverId;
}
