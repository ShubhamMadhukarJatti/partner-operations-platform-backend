package com.sharkdom.subscription.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "t_user_subscription_plans_free_trail")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSubscriptionPlanFreeTrail extends BaseEntity {

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "number_of_seats")
    private Long numberOfSeats;

    @Column(name = "plan_price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "organization_name", length = 255)
    private String organizationName;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "organization_address", length = 500)
    private String address;

    @Column(name = "gstin_id", length = 50)
    private String gstInId;
}
