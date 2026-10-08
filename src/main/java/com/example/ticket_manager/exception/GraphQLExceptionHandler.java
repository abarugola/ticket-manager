package com.example.ticket_manager.exception;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQLExceptionHandler {

    @GraphQlExceptionHandler
    public GraphQLError handleAuthenticationCredentialsNotFound(AuthenticationCredentialsNotFoundException ex, DataFetchingEnvironment env) {
        return GraphQLError.newError()
                .errorType(ErrorType.UNAUTHORIZED)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleAuthenticationException(AuthenticationException ex, DataFetchingEnvironment env) {
        return GraphQLError.newError()
                .errorType(ErrorType.UNAUTHORIZED)
                .message("No autenticado: " + ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleAccessDeniedException(AccessDeniedException ex, DataFetchingEnvironment env) {
        return GraphQLError.newError()
                .errorType(ErrorType.FORBIDDEN)
                .message("Acceso denegado: No tienes permisos para realizar esta acción")
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleGenericException(Exception ex, DataFetchingEnvironment env) {
        return GraphQLError.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Ha ocurrido un error interno: " + ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}