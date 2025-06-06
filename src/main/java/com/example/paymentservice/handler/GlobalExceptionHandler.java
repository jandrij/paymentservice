package com.example.paymentservice.handler;

import com.example.paymentservice.dto.ErrorResponseDto;
import com.example.paymentservice.exception.BusinessValidationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handle(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponseDto errorResponseDTO = ErrorResponseDto.builder()
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponseDTO);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handle(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            String fieldName = ife.getPath().get(0).getFieldName();
            String invalidValue = ife.getValue().toString();
            Object[] validValues = ife.getTargetType().getEnumConstants();

            String message = String.format(
                    "Field '%s' has invalid value '%s'. Allowed values are: %s",
                    fieldName,
                    invalidValue,
                    Arrays.toString(validValues)
            );

            ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                    .errors(List.of(message))
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
        throw ex;
    }


    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponseDto> handle(BusinessValidationException ex) {
        ErrorResponseDto errorResponseDTO = ErrorResponseDto.builder()
                .errors(List.of(ex.getMessage()))
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponseDTO);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handle(HttpRequestMethodNotSupportedException ex) {
        ErrorResponseDto errorResponseDTO = ErrorResponseDto.builder()
                .errors(List.of("HTTP method not supported: " + ex.getMethod()))
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponseDTO);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handle(ResponseStatusException ex) {
        ErrorResponseDto errorResponseDTO = ErrorResponseDto.builder()
                .errors(List.of(ex.getReason() != null
                                ? ex.getReason()
                                : "An unexpected error occurred. Please try again later."))
                .build();
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponseDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        ErrorResponseDto errorResponseDTO = ErrorResponseDto.builder()
                .errors(List.of("An unexpected error occurred. Please try again later."))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDTO);
    }
}