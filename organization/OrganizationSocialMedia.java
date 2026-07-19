package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_social_media")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OrganizationSocialMedia extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    @Schema(name = "name", example = "FACEBOOK", description = "Mandatory field, Fetch all possible roles from configuration endpoint by config_type=SOCIAL_NAME")
    String name;
    String url;
    boolean showOnUi = true;
}
