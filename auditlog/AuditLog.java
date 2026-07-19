package com.sharkdom.entity.auditlog;


import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name= "audit_log")
public class AuditLog extends BaseEntity {

    @Column(name = "ACTION")
    private String action;

    @Column(name = "SUB_ACTION")
    private String subAction;

    @Column(name="USER_NAME")
    private String userName;

    @Column(name="ORGANIZATION_ID")
    private Long organizationId;

}
