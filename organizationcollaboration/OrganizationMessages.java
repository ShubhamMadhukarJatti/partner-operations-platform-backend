package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.constants.Flag;
import com.sharkdom.constants.LinkerType;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "organization_messages")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrganizationMessages extends BaseEntity {
    private Long chatRoomId;
    private String query;
    private Long linkerId;
    private LinkerType linkerType;
    private Flag flag;
    @Enumerated(EnumType.STRING)
    private ChannelFlag channelFlag;
    private boolean isRead = false;
    private LocalDateTime readAt;
    private Long senderId;
    private Long receiverId;
    private boolean isEncrypted = false;
    @Transient
    private String benefit;
    @Transient
    private String description;
}
