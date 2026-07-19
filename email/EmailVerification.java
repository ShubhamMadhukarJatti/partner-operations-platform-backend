package com.sharkdom.entity.email;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "email_verification")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EmailVerification extends BaseEntity {
    private Long organizationId;
    private String userId;
    @Column(unique = true)
    private String verificationCode;
    @Column(unique = true)
    private String transactionId;
    private Date expiresAt;
    private boolean used = false;
    @Column
    private String email;
}
