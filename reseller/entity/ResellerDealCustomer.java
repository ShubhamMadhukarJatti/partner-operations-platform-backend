package com.sharkdom.reseller.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_reseller_deal_customers")
public class ResellerDealCustomer extends BaseEntity {

    @Column(name = "email")
    private String email;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name="reseller_deal_id")
    private Long resellerDealId;

}
