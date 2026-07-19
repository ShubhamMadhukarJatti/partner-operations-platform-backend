package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.constants.LinkerType;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.organizatiocollaboration.EditHistoryStatus;
import com.sharkdom.model.organizatiocollaboration.ProposalEditMode;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "proposal_edit_history")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProposalEditHistoryEntity extends BaseEntity {
    private Long organizationCollaborationId;
    private String benefit;
    private String description;
    private LinkerType linkerType;
    private Long senderOrganizationId;
    private Long receiverOrganizationId;
    private Long originalBenefitId;
    private Long parentId;
    private ProposalEditMode mode;
    private EditHistoryStatus historyStatus;
}
