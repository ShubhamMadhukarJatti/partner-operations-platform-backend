package com.sharkdom.repository.suggestion;

import com.sharkdom.entity.suggestion.SuggestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionRepository extends JpaRepository<SuggestionEntity, Long> {
}
