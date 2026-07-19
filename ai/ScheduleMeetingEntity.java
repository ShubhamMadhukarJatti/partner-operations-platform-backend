package com.sharkdom.entity.ai;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;

@Entity
@Table(name = "chat_bot_meeting")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleMeetingEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    private String company;
    private String purpose;
    private String date;
    private String time;
    private String email;
    private String userId;
    private Integer companySize;
    private String name;

}
