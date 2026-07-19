package com.sharkdom.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.chatbot.config.ChatbotCookieConfig;
import com.sharkdom.chatbot.dto.ChatbotRequestDto;
import com.sharkdom.chatbot.dto.ChatbotResponseDto;
import com.sharkdom.util.SharkdomApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SharkdomChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CHATBOT_URL =
            "https://sharkdom-chatbot.azurewebsites.net/chat";

    public SharkdomApiResponse<ChatbotResponseDto> chat(ChatbotRequestDto requestDto) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.COOKIE, ChatbotCookieConfig.CHATBOT_COOKIE);

        HttpEntity<ChatbotRequestDto> entity =
                new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(CHATBOT_URL, entity, String.class);

            // Parse JSON and extract only "response"
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String rawMessage = rootNode.path("response").asText();

            String cleanMessage = cleanChatbotResponse(rawMessage);

            ChatbotResponseDto responseDto =
                    new ChatbotResponseDto(cleanMessage);

            return new SharkdomApiResponse<>(
                    true,
                    "Chatbot response fetched successfully",
                    responseDto
            );

        } catch (Exception ex) {
            log.error("Error fetching chatbot response", ex);
            return new SharkdomApiResponse<>(
                    false,
                    "Failed to fetch chatbot response",
                    null
            );
        }
    }

    private String cleanChatbotResponse(String text) {
        if (text == null) return null;

        return text
                .replace("\\n", "\n")          // convert escaped newlines
                .replace("\\\"", "\"")         // remove escaped quotes
                .replace("\\’", "’")           // fix smart quotes
                .replace("\\—", "—")           // fix em dash
                .trim();
    }

}
