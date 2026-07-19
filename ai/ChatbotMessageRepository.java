package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.ChatbotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotEntity, Long> {
}
