package com.example.ticket_manager.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ApiError(ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleApiException(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ApiError("Bad request", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Valor invalido para el parametro: " + ex.getName();

        var error = new FieldErrorResponse(
                ex.getName(),
                message
        );

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        "Bad request",
                        List.of(error)
                ));
    }

}
