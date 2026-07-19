package com.sharkdom.service.meetings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class VideoSdkService {
    @Value("${video.sdk.token}")
    private String sdkToken;
    @Value("${api-url}")
    String apiUrl;

    public String generateMeeting() {
        String sdkUrl = "https://api.videosdk.live/v2/rooms";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", sdkToken);

        String jsonString = """
                {
                  "autoCloseConfig": {
                    "type": "session-end-and-deactivate",
                    "duration": 30
                  },
                  "webhook": {
                    "endPoint": "%s",
                    "events": [
                      "participant-joined",
                      "participant-left",
                      "session-started",
                      "session-ended"
                    ]
                  }
                }""";
        String endPointValue = apiUrl + "meetings/callback";

        // Format the JSON string with the endPoint value
        String requestBody = String.format(jsonString, endPointValue);


        // Create the HTTP entity with headers and body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Make the HTTP POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                sdkUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Handle the response
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode responseJson = objectMapper.readTree(responseBody);
                return responseJson.get("roomId").asText();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            log.error("unable to generate meeting link");
            return null;
        }

    }

    public void deactivateMeeting(String roomId) {
        String apiUrl = "https://api.videosdk.live/v2/rooms/deactivate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", sdkToken);
        String requestBody = "{\"roomId\": \"" + roomId + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            log.info("roomId {} deactivated", roomId);
        }

    }

}
