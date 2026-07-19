package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.SharkqQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharkqQueryRepository extends JpaRepository<SharkqQueryEntity, Long> {
    List<SharkqQueryEntity> findByOrganizationId(Long organizationId);

}
