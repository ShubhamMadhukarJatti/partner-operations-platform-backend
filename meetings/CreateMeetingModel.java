package com.sharkdom.model.meetings;

import com.sharkdom.entity.meetings.Availability;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CreateMeetingModel {
    private Long id;
    private Long senderOrganizationId;
    private Long receiverOrganizationId;
    private String title;
    private String description;
    private Date meetingTime;
    private Long rescheduledBy;
    private Long cancelledBy;
    private List<Availability> availability;
}
