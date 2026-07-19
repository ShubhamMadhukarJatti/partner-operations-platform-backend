package com.sharkdom.entity.organizationcollaboration;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.meetings.MeetingDetails;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "organization_collaboration",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"senderOrganizationId", "receiverOrganizationId"})})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrganizationCollaboration extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private long senderOrganizationId;
    private long receiverOrganizationId;
    private String senderOrganizationName;
    private String receiverOrganizationName;
    private String senderUserId;
    private String acceptorUserId;
    private String status;
    private String senderUrlsJson;
    private String receiverUrlsJson;
    private boolean chatAccessAllowed = false;
    private String contactPersonUserId;
    @Transient
    private CollaborationCategory collaborationCategory;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization__collaboration_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<PartnershipMouVersion> partnershipMouVersions;
    @Transient
    private List<MeetingDetails> meetingDetails;
    @Transient
    private String senderLogo;
    @Transient
    private String receiverLogo;
    @Transient
    private boolean senderPersonaCreated;
    @Transient
    private boolean isActivation;
    @Transient
    private boolean isOutcome;
    @Transient
    private boolean isMailBoxClaimed;
}
