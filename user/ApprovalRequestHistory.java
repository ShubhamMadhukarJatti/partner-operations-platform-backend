package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "approval_request_history",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "requestType"})},
        indexes = {@Index(columnList = "userId", name = "user_userId"),
                @Index(columnList = "requestType", name = "ap_req_request_type")})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ApprovalRequestHistory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private ApprovalRequestHistoryStatus status;
    private String requestType;
    private String userId;
    private String remarks;
    private String additionalInfo;
    private String actionBy;

}
