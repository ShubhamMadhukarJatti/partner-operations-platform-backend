package com.sharkdom.controller.ppi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sharkdom.service.ppi.PpiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/receive-webhook")
public class PpiWebbookController {


    @Autowired
    PpiService ppiService;



    @PostMapping("/{orgId}")
    public ResponseEntity<String> receiveQuestion(
            @PathVariable("orgId") Long orgId,
            @RequestBody String payload
    ) throws JsonProcessingException {
        // Optionally log or validate ID
        log.info("Received question: {}", payload);
        ppiService.webhookResponseSave(payload, orgId);
//        questionRepo.save(payload);

        return ResponseEntity.ok("Question saved");
    }


}
