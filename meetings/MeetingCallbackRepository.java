package com.sharkdom.repository.meetings;

import com.sharkdom.entity.meetings.CallbackData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingCallbackRepository extends JpaRepository<CallbackData, Long> {
}
