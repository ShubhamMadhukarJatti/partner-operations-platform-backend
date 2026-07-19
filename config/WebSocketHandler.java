package com.sharkdom.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    // Store WebSocket sessions with userId as the key
    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract userId from the WebSocket session (assume it's a query parameter or part of the session handshake)
        Long userId = getUserIdFromSession(session);
        userSessions.put(userId, session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages if necessary
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the user's session on WebSocket disconnect
        Long userId = getUserIdFromSession(session);
        userSessions.remove(userId);
    }

    // Utility function to get userId from session (you can adapt this depending on your method)
    private long getUserIdFromSession(WebSocketSession session) {
        // For example, if the userId is passed as a query parameter, you could extract it like this:
        return Long.parseLong(session.getUri().getQuery().split("=")[1]);
    }

    public void sendMessageToUser(Long userId, Notification notification) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateAdapter(TimeZone.getTimeZone("Asia/Kolkata")))
                .create();
        var message = gson.toJson(notification);
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error(ErrorMessages.SH134.getMessage(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 30000)  // 30 seconds interval
    public void sendPingToUsers() {
        userSessions.forEach((userId, session) -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("ping"));
                }
            } catch (Exception e) {
                log.error(ErrorMessages.SH135.getMessage(), e.getMessage());
            }
        });
    }
}

