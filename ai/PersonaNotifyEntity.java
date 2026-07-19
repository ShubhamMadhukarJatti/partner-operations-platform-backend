package com.sharkdom.entity.ai;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "persona_notify")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaNotifyEntity extends BaseEntity {
    private Long senderOrganizationId;
    private Long receiverOrganizationId;
    private String eventType;
    private LocalDate sentAt;
    private Date openedAt;
    private Date clickedAt;
    private String clickedLink;

}
