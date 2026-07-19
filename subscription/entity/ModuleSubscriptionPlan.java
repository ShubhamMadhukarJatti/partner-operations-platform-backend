package com.sharkdom.subscription.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "t_module_subscription_plans")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModuleSubscriptionPlan extends BaseEntity {

    @Column(name = "organization_id")
    private Long orgId;

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

    @Column(name="email")
    private String email;
}