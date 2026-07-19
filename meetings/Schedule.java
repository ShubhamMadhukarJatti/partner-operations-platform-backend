package com.sharkdom.entity.meetings;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "schedule")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule extends BaseEntity {
    private Long meetingDetailsId;
    private Date time;
    private Long scheduledWith;
    private String title;
    private String description;
    private String status;
    private String meetingLink;
    private String meetingStatus;
}
