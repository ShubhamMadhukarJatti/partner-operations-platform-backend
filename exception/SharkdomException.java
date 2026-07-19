package com.sharkdom.exception;

import com.sharkdom.constants.ErrorMessages;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SharkdomException extends RuntimeException {
	private final ErrorMessages errorMessage;
	private final String formattedMessage;

	public SharkdomException(ErrorMessages errorMessage) {
		this.errorMessage = errorMessage;
		this.formattedMessage = errorMessage.getMessage();
	}

	public SharkdomException(ErrorMessages errorMessage, Object... args) {
		this.errorMessage = errorMessage;
		this.formattedMessage = errorMessage.format(args);
	}

	@Override
	public String getMessage() {
		return formattedMessage;
	}
}