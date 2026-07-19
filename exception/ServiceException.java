package com.sharkdom.exception;

import com.sharkdom.constants.ErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceException extends RuntimeException {
    private final ErrorMessages errorMessage;
    private final String formattedMessage;

    public ServiceException(ErrorMessages errorMessage) {
        this.errorMessage = errorMessage;
        this.formattedMessage = errorMessage.getMessage();
    }

    public ServiceException(ErrorMessages errorMessage, Object... args) {
        this.errorMessage = errorMessage;
        this.formattedMessage = errorMessage.format(args);
    }

    @Override
    public String getMessage() {
        return formattedMessage;
    }
}
