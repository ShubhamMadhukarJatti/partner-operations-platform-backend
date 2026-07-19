package com.sharkdom.controller.ai;

import com.sharkdom.entity.ai.SharkqQueryEntity;
import com.sharkdom.model.ai.ChatbotMessage;
import com.sharkdom.model.ai.MeetingSchedule;
import com.sharkdom.model.ai.SharkqQueryRequest;
import com.sharkdom.service.ai.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping("/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }


    @Operation(summary = "Send message")
    @PostMapping(path = "")
    public Object chatBot(@RequestBody ChatbotMessage chatbotMessage) {
        return chatbotService.sendMessage(chatbotMessage);
    }

    @Operation(summary = "Schedule meeting")
    @PostMapping(path = "/schedule-meeting")
    public Object chatBot(@RequestBody MeetingSchedule meetingSchedule) {
        return chatbotService.scheduleMeeting(meetingSchedule);
    }

    @PostMapping("/sharkq-query")
    public Map<String, Object> sharkqQuery(@RequestBody SharkqQueryRequest message) {
        return chatbotService.sharkqQuery(message);
    }

    @GetMapping("/sharkq-query")
    public ResponseEntity<List<SharkqQueryEntity>> sharkqQuery(){
        return ResponseEntity.ok(chatbotService.getSharkqQuery());
    }
    @GetMapping("/sharkq-query/id")
    public ResponseEntity<SharkqQueryEntity> sharkqQueryById(@RequestParam Long id){
        return ResponseEntity.ok(chatbotService.getSharkqQueryById(id));
    }
}