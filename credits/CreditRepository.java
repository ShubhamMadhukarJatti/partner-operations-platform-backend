package com.sharkdom.repository.credits;

import com.sharkdom.entity.credits.Credit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long> {

    Optional<Credit> findByOrgId(Long orgId);


    @Query("SELECT c.credits FROM Credit c WHERE c.orgId = :orgId")
    Optional<Integer> findCreditsByOrgId(@Param("orgId") Long orgId);

}