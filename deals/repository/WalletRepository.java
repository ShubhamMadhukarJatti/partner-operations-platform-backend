package com.sharkdom.deals.repository;

import com.sharkdom.deals.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    WalletEntity findByOrganizationId(Long organizationId);
}
