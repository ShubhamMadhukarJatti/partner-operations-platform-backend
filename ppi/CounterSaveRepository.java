package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.CounterSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterSaveRepository extends JpaRepository<CounterSave,Long> {

    List<CounterSave> findAllByOrgId(Long orgId);
    List<CounterSave> findByOrgIdAndFormId(Long orgId, String formId);
}
