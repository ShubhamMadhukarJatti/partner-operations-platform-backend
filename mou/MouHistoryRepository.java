package com.sharkdom.repository.mou;

import com.sharkdom.entity.mou.MouHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MouHistoryRepository extends JpaRepository<MouHistory, Long> {
    Optional<MouHistory> findByOrganizationIdAndOrganizationCollaborationId(Long organizationId, Long organizationCollaborationId);

    List<MouHistory> findAllByOrganizationIdAndSigned(Long organizationId, boolean signed);

    //MouHistory findByOrganizationId(Long organizationId);

}
