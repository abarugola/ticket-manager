package com.example.ticket_manager.exception;

public record FieldErrorResponse(String field, String defaultMessage) {
}
