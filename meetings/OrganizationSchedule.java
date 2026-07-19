package com.sharkdom.model.meetings;

import com.sharkdom.entity.meetings.Availability;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class OrganizationSchedule {
    private Long id;
    private Date creationTimestamp;
    private Date lastUpdatedTimestamp;
    private Date time;
    private Long meetingDetailsId;
    private Long scheduledWith;
    private String title;
    private String description;
    private String status;
    private String meetingLink;
    private String meetingStatus;
    private Long senderOrganizationId;
    private Long receiverOrganizationId;
    private Date meetingTime;
    private String roomId;
    private Long organizationCollaborationId;
    private Long scheduledBy;
    private Long rescheduledBy;
    private Long cancelledBy;
    private List<Availability> availability;
}
