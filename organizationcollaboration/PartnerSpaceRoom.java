package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "partner_space")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerSpaceRoom extends BaseEntity {

    private String spaceName;
    private Long partnerCreated;

    @ElementCollection
    @CollectionTable(name = "partner_space_joined_orgs", joinColumns = @JoinColumn(name = "partner_space_id"))
    @Column(name = "organization_id")
    private List<Integer> partnerJoined;

    @Enumerated(EnumType.STRING)
    private SpaceType spaceType;
    private Long chatRoomId;
    private Notification notificationSettings;

    @Transient
    private Integer totalMembers;
    @Transient
    private List<Long> partnerOrgIds;
    @Transient
    private String creatorRole;

    @PostLoad
    private void calculateDerivedFields() {
        this.partnerOrgIds = new ArrayList<>();
        this.totalMembers = 0;
        this.creatorRole = "ADMIN";

        if (this.partnerCreated != null) {
            this.partnerOrgIds.add(this.partnerCreated);
            this.totalMembers++;
        }
        if (this.partnerJoined != null && !this.partnerJoined.isEmpty()) {
            for (Integer partnerId : this.partnerJoined) {
                if (partnerId != null) {
                    Long partnerIdLong = partnerId.longValue();
                    if (!partnerOrgIds.contains(partnerIdLong)) {
                        this.partnerOrgIds.add(partnerIdLong);
                        this.totalMembers++;
                    }
                }
            }
        }
    }
}
