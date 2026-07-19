package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.FormApplyStatus;
import com.sharkdom.entity.ppi.FormDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormApplyStatusRepository extends JpaRepository<FormApplyStatus,Long> {
    Optional<FormApplyStatus> findByFormIdAndAppliedOrgId(Long formId, Long appliedOrgId);
}
