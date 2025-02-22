package com.devcourse.springbootboardjpahi.advice;

import com.devcourse.springbootboardjpahi.message.ControllerAdviceExceptionMessage;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> errors = e.getFieldErrors();
        String message = errors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ControllerAdviceExceptionMessage.INVALID_ARGUMENT);

        ErrorResponse errorResponse = new ErrorResponse(message);

        return ResponseEntity.badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());

        return ResponseEntity.badRequest()
                .body(errorResponse);
    }
}
