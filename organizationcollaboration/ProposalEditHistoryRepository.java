package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.ProposalEditHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalEditHistoryRepository extends JpaRepository<ProposalEditHistoryEntity, Long> {

    List<ProposalEditHistoryEntity> findByOrganizationCollaborationIdOrderByCreationTimestampDesc(Long collaborationId);
}
