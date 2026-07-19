package com.sharkdom.partnertraining.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveQuizRequest {

    private String title;
    private List<QuestionRequest> questions;

    @Data
    public static class QuestionRequest {
        private String question;
        private List<String> options;
        private String correctAnswer;
    }
}