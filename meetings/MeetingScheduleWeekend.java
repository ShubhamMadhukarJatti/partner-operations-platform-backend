package com.sharkdom.entity.meetings;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "meeting_schedule_weekend")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingScheduleWeekend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonBackReference
    private Long id;

    private Date validFrom;

    private Date validTo;

}
