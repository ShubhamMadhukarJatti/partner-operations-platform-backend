package com.sharkdom.entity.meetings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.constants.meeting.MeetingApps;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "meeting_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Schema(name = "id", example = "1", description = "Auto generated Primary key, never send in POST request if you are trying to send new object, must sent in POST/PUT request if you are updating an existing object.")
    private Long id;

    @CreationTimestamp
    @Column(name = "creationTimestamp")
    @Schema(name = "creationTimestamp", description = "Don't send in POST request")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date creationTimestamp;

    @UpdateTimestamp
    @Column(name = "lastUpdatedTimestamp")
    @Schema(name = "lastUpdatedTimestamp", description = "Don't send in POST/PUT request")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date lastUpdatedTimestamp;

    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long senderOrganizationId;
    private Long receiverOrganizationId;
    private MeetingApps meetingApp;
    private String status;
    private String meetLink;
    private LocalDate meetDate;

}
