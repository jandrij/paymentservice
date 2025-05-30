package com.example.paymentservice.exception;

import com.example.paymentservice.dto.ErrorResponseDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handle(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponseDTO);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handle(HttpMessageNotReadableException ex) {
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

            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                    .errors(List.of(message))
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
        throw ex;
    }


    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handle(BusinessValidationException ex) {
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .errors(List.of(ex.getMessage()))
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponseDTO);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handle(HttpRequestMethodNotSupportedException ex) {
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .errors(List.of("HTTP method not supported: " + ex.getMethod()))
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponseDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .errors(List.of("An unexpected error occurred. Please try again later."))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDTO);
    }
}