package com.sharkdom.entity.email;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "email_statistics")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EmailStatistics extends BaseEntity {
    private String eventType;
    private String email;
    private String templateCode;
    private String subject;
    private Date openedAt;
    private Date clickedAt;
    private String clickedLink;
    private String env;
    private LocalDate sentAt;
}
