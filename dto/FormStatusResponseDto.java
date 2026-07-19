package com.sharkdom.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormStatusResponseDto {
    private String status;
    private LocalDateTime time;
}
