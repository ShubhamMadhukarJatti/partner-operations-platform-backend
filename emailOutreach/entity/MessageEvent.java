package com.sharkdom.emailOutreach.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(
        name = "message_events",
        indexes = {
                @Index(name = "idx_msgid", columnList = "message_id"),
                @Index(name = "idx_msg_id", columnList = "msg_id"),
                @Index(name = "idx_event", columnList = "event"),
                @Index(name = "idx_recipient", columnList = "recipient")
        }
)
@Data
public class MessageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="event_id", unique = true, nullable = false)
    private String eventIdentifier;   // not "eventId"


    @Column(nullable = false)
    private Long ts;

    private String domain;
    private String event;
    private String recipient;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "msg_id")
    private String msgId;

    private String tag;
    private String url;
    private String severity;
    private String reason;

    @Column(name = "error")
    private String errorMsg;

    @Column(name = "code")
    private String code;

    private String country;
    private String city;
    private String ip;
    private String device;
    private String ua;

    @Lob
    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;
}
