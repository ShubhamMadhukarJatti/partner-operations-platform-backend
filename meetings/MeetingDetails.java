package com.sharkdom.entity.meetings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "meeting_details")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDetails extends BaseEntity {
    private Long senderOrganizationId;
    private Long receiverOrganizationId;
    private String title;
    private String description;
    private Date meetingTime;
    private String status;
    private String meetingLink;
    private String roomId;
    private Long organizationCollaborationId;
    private Long scheduledBy;
    private Long rescheduledBy;
    private Long cancelledBy;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "meeting_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Availability> availability;
    private boolean reminderSent;
}
