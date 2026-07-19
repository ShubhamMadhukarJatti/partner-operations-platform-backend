package com.sharkdom.entity.meetings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "meeting_webhook_data")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookData extends BaseEntity {
    private String roomId;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "meeting_webhook_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Participant> participants;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "meeting_webhook_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Session> sessions;
}
