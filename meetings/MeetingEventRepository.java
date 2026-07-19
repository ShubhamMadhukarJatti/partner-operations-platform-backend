package com.sharkdom.repository.meetings;

import com.sharkdom.entity.meetings.MeetingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MeetingEventRepository extends JpaRepository<MeetingEvent, Long> {
    // Today
    List<MeetingEvent> findByMeetDate(LocalDate date);


    // Between two dates (useful for tomorrow, week, month)
    List<MeetingEvent> findByMeetDateBetween(LocalDate startDate, LocalDate endDate);


    @Query(value = "select * from meeting_event m where (m.receiver_organization_id = :orgId or m.sender_organization_id = :orgId) and (m.meet_date = :date)", nativeQuery = true)
    List<MeetingEvent> findByOrganizationMeetDate(@Param("orgId") Long orgId, @Param("date") LocalDate date);

    @Query(value = "SELECT * FROM meeting_event m " +
            "WHERE (m.receiver_organization_id = :orgId OR m.sender_organization_id = :orgId) " +
            "AND (m.meet_date BETWEEN :startDate AND :endDate)", nativeQuery = true)
    List<MeetingEvent> findByOrganizationMeetDateBetween(@Param("orgId") long organizationId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
