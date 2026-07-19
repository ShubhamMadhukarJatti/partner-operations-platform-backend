package com.sharkdom.deals.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "wallet")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletEntity extends BaseEntity {
    private Long organizationId;
    private double availableAmount;
    private double totalAmount;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<WalletTransactionEntity> transactions;
}
