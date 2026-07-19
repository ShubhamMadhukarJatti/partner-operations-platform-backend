package com.sharkdom.agenticai.controller;

import com.sharkdom.agenticai.model.ChatRequest;
import com.sharkdom.agenticai.service.DweepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.util.Map;

@RestController
@RequestMapping("/dweep")
@RequiredArgsConstructor
@Tag(name = "dweep")
public class DweepController {

    private final DweepService dweepService;

    @Operation(summary = "Reset Session")
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset(
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(dweepService.resetSession(sessionId));
    }

    @Operation(summary = "Stream Discovery")
    @PostMapping("/chat")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody ChatRequest request) {
        StreamingResponseBody body = outputStream -> dweepService.streamChat(request, outputStream);
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(body);
    }

    @Operation(summary = "List Connections")
    @GetMapping("/list-connections")
    public ResponseEntity<Map<String, Object>> listConnections() {
        return ResponseEntity.ok(dweepService.listConnections());
    }
}