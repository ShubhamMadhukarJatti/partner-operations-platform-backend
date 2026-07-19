package com.sharkdom.partnerattribution.entities;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_shared_assets")
@Data
public class SharedAsset extends BaseEntity {

    private Long orgId;

    private Long partnerOrgId;

    private String dealId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;

    private String sharedBy;

    private Boolean isDeleted = false;
}