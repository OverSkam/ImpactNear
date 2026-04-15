package mm.projectV.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation exception [ConstraintViolationException]: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, "Validation failed", errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation exception [MethodArgumentNotValidException]: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, "Validation failed", errors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleException(NotFoundException e) {
        log.warn("Not found exception [NotFoundException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.NOT_FOUND, true, e.getMessage(), null);
    }

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<?> handleException(PermissionException e) {
        log.warn("Permission exception [PermissionException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, true, e.getMessage(), null);
    }

    @ExceptionHandler(LocationException.class)
    public ResponseEntity<?> handleException(LocationException e) {
        log.warn("Location exception [LocationException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, e.getMessage(), null);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<?> handleException(InvalidRequestException e) {
        log.warn("Invalid request exception [InvalidRequestException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, e.getMessage(), null);
    }

    @ExceptionHandler(ParticipationException.class)
    public ResponseEntity<?> handleException(ParticipationException e) {
        log.warn("Participation exception [ParticipationException]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.CONFLICT, true, e.getMessage(), null);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("Internal server error [Exception]: {}", e.getMessage());

        return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, true, "Internal server error", null);
    }
}
