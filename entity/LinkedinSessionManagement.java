package com.sharkdom.agenticai.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "linkedin_session_management")
public class LinkedinSessionManagement extends BaseEntity {

    @Column(unique = true, nullable = false, name="user_id", length = 255)
    private String userId;

    @Column(name = "org_id")
    private Long orgId;

}
