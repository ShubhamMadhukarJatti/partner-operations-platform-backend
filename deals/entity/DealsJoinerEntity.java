package com.sharkdom.deals.entity;

import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "deals_joiner")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealsJoinerEntity extends BaseEntity {
    private String dealId;
    private String userId;
    private Long organizationId;
    private ApprovalRequestHistoryStatus status;
    private String affiliateCode;

}
