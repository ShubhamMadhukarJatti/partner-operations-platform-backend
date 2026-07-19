package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.OfflineContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface OfflineContractRepository extends JpaRepository<OfflineContract, Long> {
    List<OfflineContract> getAllByOrg1EmailAndOrg2Email(String org1Email, String org2Email);

}
