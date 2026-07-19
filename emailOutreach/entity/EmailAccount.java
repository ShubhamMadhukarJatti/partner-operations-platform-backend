package com.sharkdom.emailOutreach.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "email_accounts")
public class EmailAccount extends BaseEntity {

    @Column(name="ORGANIZATION_ID",length = 255)
    private Long organizationId;

    @Column(name="IS_CLAIMED")
    private boolean isClaimed;

}
