package com.sharkdom.entity.referral;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(name = "leads", uniqueConstraints = {@UniqueConstraint(columnNames = {"email", "referralCode"})})
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadsEntity extends BaseEntity {
    private String email;
    private String referralCode;
    private String name;
    private LeadsStatus leadsStatus = LeadsStatus.NEW;

    public enum LeadsStatus {
        NEW, OPEN, UNQUALIFIED, CONNECTED, IN_PROGRESS
    }
}
