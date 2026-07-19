package com.sharkdom.deals.repository;

import com.sharkdom.deals.entity.WalletEntity;
import com.sharkdom.deals.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {

    List<WalletTransactionEntity> findByWalletAndType(WalletEntity wallet, String type);

}
