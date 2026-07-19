package com.sharkdom.zoho.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "zoho_connections")
@Getter
@Setter
public class ZohoConnection extends BaseEntity{

    private Long tenantId;

    @Column(length = 4000)
    private String accessToken;

    @Column(length = 4000)
    private String refreshToken;

    private String apiDomain;
}
