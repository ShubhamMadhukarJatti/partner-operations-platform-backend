package com.sharkdom.repository.subscription;


import com.sharkdom.constants.subscription.SubscriptionStatus;
import com.sharkdom.entity.subscription.Subscription;
import com.sharkdom.model.subscription.SubscriptionExpiring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByUserId(String userId);

    List<Subscription> findAllByOrganizationId(Long organizationId);

    Optional<Subscription> findFirstByOrganizationIdOrderByCreationTimestampDesc(Long organizationId);

    List<Subscription> findAllByUserIdAndStatusIn(String userId, List<SubscriptionStatus> status);

    List<Subscription> findAllByOrganizationIdAndStatusIn(long organizationId, List<SubscriptionStatus> status);

    List<Subscription> findAllByStatusIn(List<SubscriptionStatus> status);

    List<Subscription> findAllByPaymentFkAndStatusIn(long paymentFk, List<SubscriptionStatus> status);

    boolean existsSubscriptionByTransactionId(String transactionId);

    @Query(value = "SELECT\n" +
            "\tDISTINCT organizationId as organizationId, \n" +
            "\tendOn as endOn, \n" +
            "\tplanCode as planCode\n" +
            "FROM\n" +
            "\t Subscription s\n" +
            "where\n" +
            "\tFUNCTION('DATE', s.endOn) = FUNCTION('DATE', :daysAgo)")
    List<SubscriptionExpiring> findOrgIdForSubscriptionExpiriration(LocalDate daysAgo);

    Optional<Subscription> findByOrganizationIdAndPlanCode(long organizationId, String name);

    Subscription findByTransactionId(String paymentId);
}