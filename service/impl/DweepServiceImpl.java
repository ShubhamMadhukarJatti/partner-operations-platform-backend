package com.sharkdom.agenticai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.agenticai.confignew.DweepAgentConfig;
import com.sharkdom.agenticai.model.ChatRequest;
import com.sharkdom.agenticai.service.DweepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DweepServiceImpl implements DweepService {

    private final DweepAgentConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> resetSession(String sessionId) {
        String url = config.getBaseUrl() + "/dweep/reset?session_id=" + sessionId;
        return restTemplate.postForObject(url, null, Map.class);
    }

    @Override
    public void streamChat(ChatRequest request, OutputStream outputStream) {
        String url = config.getBaseUrl() + "/dweep/chat";
        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(request);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize chat request", ex);
        }

        try {
            restTemplate.execute(
                url,
                HttpMethod.POST,
                clientRequest -> {
                    clientRequest.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    clientRequest.getHeaders().setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                    clientRequest.getBody().write(body);
                },
                clientResponse -> {
                    if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                        String errorBody = StreamUtils.copyToString(
                            clientResponse.getBody(), StandardCharsets.UTF_8);
                        throw new RestClientResponseException(
                            "Dweep chat request failed",
                            clientResponse.getStatusCode().value(),
                            clientResponse.getStatusText(),
                            clientResponse.getHeaders(),
                            errorBody.getBytes(StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8
                        );
                    }
                    StreamUtils.copy(clientResponse.getBody(), outputStream);
                    return null;
                }
            );
        } catch (RestClientResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to stream Dweep chat response", ex);
        }
    }

    @Override
    public Map<String, Object> listConnections() {
        String url = config.getBaseUrl() + "/dweep/list-connections";
        return restTemplate.getForObject(url, Map.class);
    }
}