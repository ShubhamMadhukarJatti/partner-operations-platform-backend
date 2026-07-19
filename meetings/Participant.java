package com.sharkdom.entity.meetings;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "participant")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant extends BaseEntity {
    private String sessionId;
    private String participantId;
    private String participantName;
    private Date joinedAt;
    private Date leftAt;
}
