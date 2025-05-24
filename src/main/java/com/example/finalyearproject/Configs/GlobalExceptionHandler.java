package com.example.finalyearproject.Configs;

import com.example.finalyearproject.Utility.ApiResponse;
import com.example.finalyearproject.customExceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handles @Valid failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();
        Map<String, String> errorDetails = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorDetails.put(error.getField(), error.getDefaultMessage());
            errorMessages.add(error.getField() + ": " + error.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("Validation failed", errorDetails, errorMessages));
    }

    // Handles @Validated violations (e.g., from query params)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errorMessages = new ArrayList<>();
        Map<String, String> errorDetails = new HashMap<>();

        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            errorDetails.put(field, cv.getMessage());
            errorMessages.add(field + ": " + cv.getMessage());
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("Validation constraints violated", errorDetails, errorMessages));
    }

    // Handle database constraint violations (like duplicate phone/email)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String errorMessage = ex.getMostSpecificCause().getMessage().toLowerCase();

        // Log the original error for debugging
        logger.debug("Database constraint violation: {}", errorMessage);

        // Phone number constraints - check for various patterns that might indicate phone number issues
        if (errorMessage.contains("phone") ||
                errorMessage.contains("consumer_phone") ||
                errorMessage.contains("farmer_phone") ||
                (errorMessage.contains("duplicate") && errorMessage.contains("919535991365"))) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Registration failed", "This phone number is already registered"));
        }
        // Email constraints
        else if (errorMessage.contains("email") ||
                errorMessage.contains("consumer_email") ||
                errorMessage.contains("farmer_email")) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Registration failed", "This email is already registered"));
        }

        // For other constraint violations, create a more helpful message
        String fieldName = extractFieldNameFromError(errorMessage);
        String userMessage = fieldName != null ?
                "A record with this " + fieldName + " already exists" :
                "A record with this information already exists";

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Data validation error", userMessage));
    }

    // Handle transaction exceptions which might wrap constraint violations
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponse<Object>> handleTransactionSystemException(TransactionSystemException ex) {
        Throwable cause = ex.getRootCause();

        if (cause instanceof DataIntegrityViolationException) {
            return handleDataIntegrityViolation((DataIntegrityViolationException) cause);
        }

        if (cause instanceof ConstraintViolationException) {
            return handleConstraintViolation((ConstraintViolationException) cause);
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Transaction failed", "The operation couldn't be completed"));
    }

    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Resource not found", ex.getMessage()));
    }

    // Catch-all exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllOtherExceptions(Exception ex) {
        logger.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "Please try again later"));
    }

    // Helper method to extract field names from error messages
    private String extractFieldNameFromError(String errorMessage) {
        // Try to extract field name from common patterns in constraint violation messages
        if (errorMessage.contains("duplicate") || errorMessage.contains("unique")) {
            // Common field names to check for
            for (String field : Arrays.asList("name", "username", "address", "id", "code", "number")) {
                if (errorMessage.contains(field)) {
                    return field;
                }
            }

            // Try to extract field from UK_ pattern in MySQL errors
            int ukIndex = errorMessage.indexOf("uk_");
            if (ukIndex >= 0) {
                int nextSpaceIndex = errorMessage.indexOf(" ", ukIndex);
                if (nextSpaceIndex > ukIndex) {
                    return "unique field";
                }
            }
        }
        return null;
    }
}