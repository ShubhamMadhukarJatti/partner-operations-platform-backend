package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.DynamicTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DynamicTableRepository extends JpaRepository<DynamicTable, Long> {

    Optional<DynamicTable> findByOrgIdAndName(Long orgId, String name);

    Optional<DynamicTable> findByOrgIdAndEmail(Long orgId, String email);

    Optional<DynamicTable> findByEmailAndName(String email, String name);

    Optional<DynamicTable> findByIdAndOrgIdAndEmail(
            Long id,
            Long orgId,
            String email
    );

    List<DynamicTable> findByOrgId(Long orgId);

}
