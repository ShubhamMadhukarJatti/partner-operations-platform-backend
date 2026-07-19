package com.sharkdom.exception;

import lombok.Builder;

import java.util.Date;

@Builder
public record ErrorResponse(int statusCode, Date timestamp, String errorCode, String errorMessage, String description) {
}