package com.sharkdom.entity.configuration;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuration", indexes = {@Index(columnList = "config_type", name = "config_config_type"),
        @Index(columnList = "config_key", name = "config_config_key"),
        @Index(columnList = "config_value", name = "config_config_value"),
        @Index(columnList = "active", name = "config_active")})
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class Configuration extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "config_type")
    private String type;
    @Column(name = "config_key")
    private String key;
    @Column(name = "config_value")
    private String value;
    private boolean webApplicable = true;
    private boolean appApplicable = true;
    private boolean backendApplicable = true;
    private boolean active = true;

}

