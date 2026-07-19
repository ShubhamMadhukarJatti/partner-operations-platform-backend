package com.sharkdom.repository.user;

import com.sharkdom.entity.user.ProfileVisits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProfileVisitsRepository extends JpaRepository<ProfileVisits, Long> {

    @Query("FROM ProfileVisits pv WHERE pv.visitedUserId = :visitedUserId AND creationTimestamp BETWEEN :fromTimestamp AND :toTimeStamp")
    List<ProfileVisits> findAllProfileVisitsByVisitedUserIdAndAndCreationTimestampBetween(String visitedUserId,
                                                                                          Date fromTimestamp, Date toTimeStamp);

}