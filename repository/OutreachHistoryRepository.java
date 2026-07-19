package com.sharkdom.agenticai.repository;

import com.sharkdom.agenticai.entity.OutreachHistory;
import com.sharkdom.agenticai.enums.OutreachChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutreachHistoryRepository extends JpaRepository<OutreachHistory, Long> {

    Optional<OutreachHistory> findByCompanyNameAndChannelAndOrgId(
            String companyName,
            OutreachChannel channel,
            Long orgId
    );

    List<OutreachHistory> findByOrgId(Long orgId);

    long countByOrgIdAndChannel(Long orgId, OutreachChannel channel);

}
