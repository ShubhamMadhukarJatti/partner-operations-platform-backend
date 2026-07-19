package com.sharkdom.partnertraining.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizResponse {

    private Long quizId;
    private String title;
    private List<QuestionResponse> questions;

    @Data
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String question;
        private List<String> options;
        private Integer order;
    }
}