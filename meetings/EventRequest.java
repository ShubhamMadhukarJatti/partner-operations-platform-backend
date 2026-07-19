package com.sharkdom.model.meetings;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EventRequest {

    private String title;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private Long senderOrganizationId;
    private Long receiverOrganizationId;

}