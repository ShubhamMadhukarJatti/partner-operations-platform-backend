package com.sharkdom.entity.admin;

import com.sharkdom.constants.Days;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partner_alerts_status")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PartnerAlertsEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day", nullable = false, unique = true)
    private Days day;

    @Column(name = "is_disabled", nullable = false)
    private boolean isDisabled;
}
