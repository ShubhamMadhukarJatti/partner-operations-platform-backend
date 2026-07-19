package com.sharkdom.mapper;

import com.sharkdom.constants.LinkerType;
import com.sharkdom.entity.organizationcollaboration.ProposalEditHistoryEntity;
import com.sharkdom.model.organizatiocollaboration.EditHistoryStatus;
import com.sharkdom.model.organizatiocollaboration.ProposalEditDetails;

public class ProposalEditHistoryMapper {
    private ProposalEditHistoryMapper() {
    }

    public static ProposalEditHistoryEntity mapToProposalEditHistoryEntity(Long collaborationId, ProposalEditDetails request, Long senderOrganizationId, Long receiverOrganizationId, LinkerType linkerType) {
        ProposalEditHistoryEntity editEntity = new ProposalEditHistoryEntity();
        editEntity.setOrganizationCollaborationId(collaborationId);
        editEntity.setBenefit(request.benefit());
        editEntity.setDescription(request.description());
        editEntity.setLinkerType(linkerType);
        editEntity.setOriginalBenefitId(request.id());
        editEntity.setParentId(request.parentId());
        editEntity.setMode(request.mode());
        editEntity.setHistoryStatus(EditHistoryStatus.PENDING);
        editEntity.setSenderOrganizationId(senderOrganizationId);
        editEntity.setReceiverOrganizationId(receiverOrganizationId);
        return editEntity;
    }
}
