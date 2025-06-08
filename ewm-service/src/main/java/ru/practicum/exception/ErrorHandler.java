package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        return new ApiError(e.getMessage(), "The required object was not found.",
                            HttpStatus.NOT_FOUND.name(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolation(final ConstraintViolationException e) {
        return new ApiError(e.getMessage(), "For the requested operation the conditions are not met.",
                            HttpStatus.CONFLICT.name(), LocalDateTime.now());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final ConflictException e) {
        return new ApiError(e.getMessage(), "Integrity constraint has been violated.",
                            HttpStatus.CONFLICT.name(), LocalDateTime.now());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(final ValidationException e) {
        return new ApiError(e.getMessage(), "For the requested operation the conditions are not met.",
                            "BAD_REQUEST", LocalDateTime.now());
    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidate(final NumberFormatException e) {
        return new ApiError(e.getMessage(), "Incorrectly made request.",
                            HttpStatus.BAD_REQUEST.name(), LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidate(final MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = "Error";
        if (fieldError != null) {
            String field = fieldError.getField();
            String error = fieldError.getDefaultMessage();
            Object rejected = fieldError.getRejectedValue();
            String valueStr = rejected == null ? "null" : rejected.toString();
            message = String.format("Field: %s. Error: %s. Value: %s", field, error, valueStr);
        }

        return new ApiError(message, "Incorrectly made request.",
                            HttpStatus.BAD_REQUEST.name(), LocalDateTime.now());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidate(final MissingServletRequestParameterException e) {
        return new ApiError(e.getMessage(), "Incorrectly made request.",
                            HttpStatus.BAD_REQUEST.name(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        return new ApiError(e.getMessage(), "An unexpected error occurred.",
                            HttpStatus.INTERNAL_SERVER_ERROR.name(), LocalDateTime.now());
    }
}
