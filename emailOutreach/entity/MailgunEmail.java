package com.sharkdom.emailOutreach.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name = "t_mailgun_emails")
public class MailgunEmail extends BaseEntity {

        private String threadToken;
        private String fromRaw;
        private String fromEmail;
        private String toRaw;
        private String subject;
        private String textBody;

        @Column(columnDefinition = "TEXT")
        private String htmlBody;

        private String messageId;
        private String inReplyTo;
    @Column(name = "email_references")
    private String referencesField;
        private String ip;

}
