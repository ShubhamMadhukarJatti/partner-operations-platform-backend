package com.sharkdom.entity.ai;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonaUserNotifyDto {
    Long senderOrgId;
    Long receiverOrgId;
    Boolean isNotified;
}
