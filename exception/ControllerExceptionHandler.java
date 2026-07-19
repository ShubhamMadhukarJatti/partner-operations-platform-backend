package com.sharkdom.exception;

import com.sharkdom.constants.ErrorMessages;
import com.stripe.exception.SignatureVerificationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.error("Sharkdom Exception {}", ex.getErrorMessage().getMessage(), ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .statusCode(ex.getErrorMessage().getHttpStatus().value())
                .timestamp(new Date())
                .description(request.getDescription(false))
                .errorCode(ex.getErrorMessage().name())
                .errorMessage(ex.getMessage())
                .build(), ex.getErrorMessage().getHttpStatus());
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorMessage> resourceAlreadyExistException(ResourceAlreadyExistException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.CONFLICT.value(), new Date(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<ErrorMessage> stripeSubscriptionDataNotCreatedException(SignatureVerificationException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(), ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SharkdomException.class)
    public ResponseEntity<ErrorResponse> handleSharkdomException(SharkdomException ex, WebRequest request) {
        log.error("Sharkdom Exception {}", ex.getErrorMessage().getMessage(), ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .statusCode(ex.getErrorMessage().getHttpStatus().value())
                .timestamp(new Date())
                .description(request.getDescription(false))
                .errorCode(ex.getErrorMessage().name())
                .errorMessage(ex.getMessage())
                .build(), ex.getErrorMessage().getHttpStatus());
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        log.error("Unauthorized Exception {}", ex.getErrorMessage().getMessage(), ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .statusCode(ex.getErrorMessage().getHttpStatus().value())
                .timestamp(new Date())
                .description(request.getDescription(false))
                .errorCode(ex.getErrorMessage().name())
                .errorMessage(ex.getMessage())
                .build(), ex.getErrorMessage().getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
        log.error("error in handler {}", ex.getMessage());
        ErrorMessage message = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex, WebRequest request) {
        log.error("service exception {}", ex.getErrorMessage().getMessage(), ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .statusCode(ex.getErrorMessage().getHttpStatus().value())
                .timestamp(new Date())
                .description(request.getDescription(false))
                .errorCode(ex.getErrorMessage().name())
                .errorMessage(ex.getMessage())
                .build(), ex.getErrorMessage().getHttpStatus());
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> jsonExceptionHandler(Exception ex, WebRequest request) {
        log.error("error in handler {}", ex.getMessage());
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintsException(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream().findFirst().map(ConstraintViolation::getMessageTemplate);
        return errors.map(error -> {
            var errorMessage = ErrorMessages.valueOf(error);
            return new ResponseEntity<>(ErrorResponse.builder().errorCode(errorMessage.name()).errorMessage(errorMessage.getMessage()).build(), HttpStatus.BAD_REQUEST);
        }).orElseGet(() -> new ResponseEntity<>(ErrorResponse.builder()
                .errorCode(ErrorMessages.SH05.name())
                .errorMessage(ErrorMessages.SH05.getMessage()).build(), ErrorMessages.SH05.getHttpStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> constraintViolationHandler(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(), errors.toString(), request.getDescription(false));
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
//        var errors = ex.getBindingResult().getAllErrors().stream().findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage);
//        return errors.map(error -> {
//            var errorMessage = ErrorMessages.valueOf(error);
//            return new ResponseEntity<>(ErrorResponse.builder().errorCode(errorMessage.name()).errorMessage(errorMessage.getMessage()).build(), HttpStatus.BAD_REQUEST);
//        }).orElseGet(() -> new ResponseEntity<>(ErrorResponse.builder().errorCode(ErrorMessages.SH05.name()).errorMessage(ErrorMessages.SH04.getMessage()).build(), HttpStatus.BAD_REQUEST));

    }
}