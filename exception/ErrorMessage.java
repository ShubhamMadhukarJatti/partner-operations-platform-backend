package com.sharkdom.exception;

import lombok.Builder;

import java.util.Date;

@Builder
public record ErrorMessage(int statusCode, Date timestamp, String errorMessage, String description) {
}
