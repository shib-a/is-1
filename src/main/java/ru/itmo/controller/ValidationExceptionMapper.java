package ru.itmo.controller;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String field = cv.getPropertyPath().toString();
                    String message = cv.getMessage();
                    return field + ": " + message;
                })
                .collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errors)
                .type("application/json")
                .build();
    }
}
