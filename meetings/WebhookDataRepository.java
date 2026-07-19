package com.sharkdom.repository.meetings;

import com.sharkdom.entity.meetings.WebhookData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookDataRepository extends JpaRepository<WebhookData, Long> {
     WebhookData findByRoomId(String roomId);
}
