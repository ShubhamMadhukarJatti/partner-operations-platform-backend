package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.TimelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface TimelineRepository extends JpaRepository<TimelineEntity, Long> {
    List<TimelineEntity> getAllByOrganizationCollaborationId(long organizationCollaborationId);

}
