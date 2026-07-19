package com.sharkdom.repository.email;

import com.sharkdom.entity.email.EmailSubscribed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EmailSubscribedRepository extends JpaRepository<EmailSubscribed, Long> {
    @Query("select es from EmailSubscribed es")
    List<EmailSubscribed> findAllEmails();

    boolean existsByEmail(String email);

    @Query("SELECT e FROM EmailSubscribed e " +
            "WHERE e.lastUpdatedTimestamp >= :updateAfter " +
            "AND e.lastUpdatedTimestamp < :updateBefore")
    List<EmailSubscribed> findAllEmailsBetweenDates(
            @Param("updateAfter") Date updateAfter,
            @Param("updateBefore") Date updateBefore
    );
}
