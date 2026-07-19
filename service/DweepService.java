package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.ChatRequest;
import java.io.OutputStream;
import java.util.Map;

public interface DweepService {
    Map<String, Object> resetSession(String sessionId);
    void streamChat(ChatRequest request, OutputStream outputStream);
    Map<String, Object> listConnections();
}