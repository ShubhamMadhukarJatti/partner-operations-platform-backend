package com.sharkdom.entity.organizationcollaboration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.constants.MouStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "partnership_mou_version")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PartnershipMouVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;
    public long organizationCollaborationId;
    private String senderOrgSigner;
    private String receiverOrgSigner;
    private String senderOrgcontactPerson;
    private String receiverOrgcontactPerson;
    private MouStatus status;
    private int version;
    private String filePath;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "partnership_mou_version_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<SenderBenefit> senderBenefits;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "partnership_mou_version_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<ReceiverBenefit> receiverBenefits;
    private Date senderSignedOn;
    private Date receiverSignedOn;
    private String senderOrgmodifiedByUserId;
    private String receiverOrgmodifiedByUserId;
    private boolean isViewed = false;
    private boolean isEmailOpened = false;
    private boolean isEmailClicked = false;
}
