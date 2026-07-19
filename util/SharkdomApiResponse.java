package com.sharkdom.util;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharkdomApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
