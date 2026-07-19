package com.sharkdom.registration.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "refresh_token")
@Getter
@Setter
@Entity
public class RefreshTokenEntity extends BaseEntity {
    @Lob
    private String token;
    private LocalDateTime expiryDate;
    private String userId;

}

