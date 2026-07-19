package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_visits", indexes = {@Index(columnList = "visitedUserId", name = "visited_userId")})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProfileVisits extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String visitedUserId;
    private String visitorUsername;
    private String sourceType;
    private String sourceOrVisitorId;
    private String capturedTimeInSeconds;
    private String additionalData;
    private String visitorSectors;

}
