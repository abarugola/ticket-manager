package com.example.ticket_manager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError (
    String message,
    List<FieldErrorResponse> errors
) {}
