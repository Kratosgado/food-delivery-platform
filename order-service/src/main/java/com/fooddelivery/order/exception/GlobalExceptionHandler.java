package com.fooddelivery.order.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
    logger.error("Resource not found: {}", ex.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ProblemDetail handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
    logger.error("Resource conflict: {}", ex.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
    logger.error("Unauthorized access: {}", ex.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ProblemDetail handleForbidden(ForbiddenException ex) {
    logger.error("Forbidden access: {}", ex.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ProblemDetail handleInvalidRequest(InvalidRequestException ex) {
    logger.error("Invalid request: {}", ex.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    error -> error.getField(),
                    error ->
                        error.getDefaultMessage() != null
                            ? error.getDefaultMessage()
                            : "Invalid value",
                    (existing, replacement) -> existing));

    logger.error("Validation failed: {}", errors);
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors.toString());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
    Map<String, String> errors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> violation.getMessage(),
                    (existing, replacement) -> existing));

    logger.error("Constraint violation: {}", errors);
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors.toString());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message =
        String.format(
            "Parameter '%s' should be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

    logger.error("Type mismatch: {}", message);
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex) {
    String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

    logger.error("Missing parameter: {}", message);
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(RuntimeException.class)
  public ProblemDetail handleRuntimeException(RuntimeException ex, WebRequest request) {
    logger.error("Runtime exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGlobalException(Exception ex, WebRequest request) {
    logger.error(
        "Unhandled exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalState(IllegalStateException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }
}
