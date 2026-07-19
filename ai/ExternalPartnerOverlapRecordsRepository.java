package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.ExternalPartnerOverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.model.ai.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalPartnerOverlapRecordsRepository extends JpaRepository<ExternalPartnerOverlapRecordEntity, Long> {
    List<ExternalPartnerOverlapRecordEntity> findByUserId(String userId);

    Page<ExternalPartnerOverlapRecordEntity> findPageByUserId(String userId, Pageable pageable);

    Page<ExternalPartnerOverlapRecordEntity> findPageByUserIdAndRecordType(String userId, RecordType recordType, Pageable pageable);

    Optional<ExternalPartnerOverlapRecordEntity> findByUserIdAndRecordType(String userId, RecordType recordType);

    boolean existsByUserId(String userId);
}
