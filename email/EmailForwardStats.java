package com.sharkdom.entity.email;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_forward_stats")
@Data
public class EmailForwardStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;   // unique identifier for each email

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String subject;

    private Long senderOrgId;

    @Column(nullable = false)
    private String receiverEmail;

    private Long receiverOrgId;

    @Column(nullable = false)
    private String status; // SENT, FAILED, BOUNCED, CLICKED, OPENED
    @Lob
    private String messageBody;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
