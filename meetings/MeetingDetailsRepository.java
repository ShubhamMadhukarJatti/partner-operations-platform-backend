package com.sharkdom.repository.meetings;

import com.sharkdom.entity.meetings.MeetingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingDetailsRepository extends JpaRepository<MeetingDetails, Long> {
    MeetingDetails findByRoomId(String roomId);

    List<MeetingDetails> findByOrganizationCollaborationId(Long id);

    @Query("SELECT m FROM MeetingDetails m WHERE m.meetingTime BETWEEN :startTime AND :endTime AND m.reminderSent = false")
    List<MeetingDetails> findMeetingsWithinTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

}
