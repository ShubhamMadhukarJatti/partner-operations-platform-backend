package com.sharkdom.repository.perks;

import com.sharkdom.entity.perks.PerkStatus;
import com.sharkdom.entity.perks.PerksEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerksRepository extends JpaRepository<PerksEntity, Long> {

    Page<PerksEntity> findAllByPerkStatus(PerkStatus status, Pageable pageable);
}
