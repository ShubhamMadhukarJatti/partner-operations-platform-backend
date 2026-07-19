package com.sharkdom.emailagent.controller;

import com.sharkdom.emailagent.service.EmailAgentService;
import com.sharkdom.emailagent.dto.EmailRequest;
import com.sharkdom.emailagent.dto.EmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/email/agent")
@RequiredArgsConstructor
public class EmailAgentController {

    private final EmailAgentService emailAgentService;

    @PostMapping("/generate")
    public EmailResponse generateEmails(@RequestBody EmailRequest emailRequest) {
        log.info("Email Request received");
        return emailAgentService.generateEmails(emailRequest);
    }
}