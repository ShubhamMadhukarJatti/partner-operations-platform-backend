package com.sharkdom.entity.email;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "email_subscribed")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EmailSubscribed extends BaseEntity {
    private String email;
}
