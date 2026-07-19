package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "pending_approval_request")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PendingApprovalRequest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String status;
    private String requestType;
    private String userId;
    private String remarks;
    private String additionalInfo;

}
