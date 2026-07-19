package com.sharkdom.profilesection.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_user_sidebar_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSidebarConfig extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String pinnedItemHrefs; // store as JSON string

    @Column(columnDefinition = "TEXT")
    private String openNestedItems; // store as JSON string

    @Column(columnDefinition = "TEXT")
    private String sidebarItemHrefs; // store as JSON string

    @Column(nullable = false)
    private Boolean isCollapsed = false;

    private Boolean isPartnerView = false;

    private Boolean isVendorView = false;

}