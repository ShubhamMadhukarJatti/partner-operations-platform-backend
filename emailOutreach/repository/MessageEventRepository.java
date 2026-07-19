package com.sharkdom.emailOutreach.repository;

import com.sharkdom.emailOutreach.entity.MessageEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageEventRepository extends JpaRepository<MessageEvent, Long> {
    Optional<MessageEvent> findByEventIdentifier(String eventId);

    List<MessageEvent> findByMessageId(String messageId);
}