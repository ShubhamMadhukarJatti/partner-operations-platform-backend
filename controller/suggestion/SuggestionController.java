package com.sharkdom.controller.suggestion;

import com.sharkdom.entity.suggestion.SuggestionEntity;
import com.sharkdom.service.suggestion.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suggestion")
@RequiredArgsConstructor
public class SuggestionController {

    public final SuggestionService suggestionService;

    @Operation(summary = "Creating a suggestion")
    @PostMapping
    public ResponseEntity<SuggestionEntity> createSuggestion(@RequestBody SuggestionEntity suggestion) {
        SuggestionEntity savedSuggestion = suggestionService.createSuggestion(suggestion);
        return new ResponseEntity<>(savedSuggestion, HttpStatus.CREATED);
    }

}
