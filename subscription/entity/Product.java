package com.sharkdom.subscription.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "t_products_stripe_pricing")
public class Product extends BaseEntity {

    @Column(name = "product_id", nullable = false, unique = true)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", nullable = false)
    private ModuleName productName;

    @Column(name="price_inr")
    private Double priceINR;

    @Column(name="price_usd")
    private Double priceUSD;
}
