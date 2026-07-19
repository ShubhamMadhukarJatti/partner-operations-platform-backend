package com.sharkdom.entity.payment;

import com.sharkdom.entity.BaseEntity;
import lombok.*;
import com.phonepe.sdk.pg.payments.v1.models.response.PgPaymentInstrument.Type;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentModel extends BaseEntity {
    private String code;
    private String merchantId;
    private String merchantTransactionId;
    private String transactionId;
    private Long amount;
    private Type type;
    private Long organizationId;
}
