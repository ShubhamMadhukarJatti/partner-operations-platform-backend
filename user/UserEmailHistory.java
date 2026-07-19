package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "user_email_history")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserEmailHistory extends BaseEntity {
    String userId;
    Long senderOrganizationId;
}
