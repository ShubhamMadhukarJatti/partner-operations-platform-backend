package com.sharkdom.entity.email;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "email_update")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EmailUpdateEntity extends BaseEntity {
    private String originalEmail;
    private String newEmail;
    private String otp;
}
