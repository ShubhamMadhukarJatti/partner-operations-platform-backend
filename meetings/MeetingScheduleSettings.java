package com.sharkdom.entity.meetings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.constants.meeting.MeetingApps;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "meeting_schedule_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingScheduleSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Schema(name = "id", example = "1", description = "Auto generated Primary key, never send in POST request if you are trying to send new object, must sent in POST/PUT request if you are updating an existing object.")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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

    @Column(unique = true, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    private List<MeetingApps> connectedApps;

    @Enumerated(EnumType.STRING)
    private MeetingApps defaultApp;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "weekday_id", referencedColumnName = "id")
    private MeetingScheduleWeekdays weekDays;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "weekend_id", referencedColumnName = "id")
    private MeetingScheduleWeekend weekEnd;

}
