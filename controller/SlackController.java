package com.sharkdom.controller;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/slack/interaction")
public class SlackController {
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleSlackInteraction(@RequestParam("payload") String payload) {
        try {
            JSONObject interactionPayload = new JSONObject(payload);
            String actionId = interactionPayload
                    .getJSONArray("actions")
                    .getJSONObject(0)
                    .getString("action_id");

            if ("open_preferences".equals(actionId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("response_type", "ephemeral");
                response.put("text", "Opening preferences in Sharkdom...");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("response_type", "ephemeral");
                response.put("text", "Unknown action.");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("text", "Error processing interaction."));
        }
    }
}
