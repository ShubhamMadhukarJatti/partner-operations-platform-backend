package com.sharkdom.deals.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transaction")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private WalletEntity wallet;
    private String userId;
    private double amount;
    private String type;
    private LocalDateTime timestamp;

    private String orderId;
    private String paymentId;
}

