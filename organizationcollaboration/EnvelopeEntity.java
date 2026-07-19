package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "envelope")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EnvelopeEntity extends BaseEntity {
    private String subject;
    private String envelopeId;
    private LocalDate dateSent;
    private LocalDate dateCreated;
    private LocalDate statusDate;
    private String status;
    private String holder;
    private boolean signedBySender;
    private boolean signedByReceiver;
}
