package com.sharkdom.deals.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "commission_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRequestEntity extends BaseEntity {

    private Long organizationId;
    private Long requestingOrganizationId;
    private String requestingOrganizationName;
    private String status;
    private Double amount;
    private LocalDate date;
    private String rejectingReason;
    private String name;
    private String invoiceAzure;
    private String transactionId;
    private Integer commissionPercentage;
}
