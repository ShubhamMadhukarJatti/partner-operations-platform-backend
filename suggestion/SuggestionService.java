package com.sharkdom.service.suggestion;

import com.sharkdom.entity.suggestion.SuggestionEntity;
import com.sharkdom.repository.suggestion.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;

    public SuggestionEntity createSuggestion(SuggestionEntity suggestion) {
        return suggestionRepository.save(suggestion);
    }

}
