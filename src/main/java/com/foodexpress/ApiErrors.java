package com.foodexpress;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<?> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Request failed":e.getReason()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("message",e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).findFirst().orElse("Invalid request")));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<?> malformed() {
        return ResponseEntity.badRequest().body(Map.of("message","Invalid request body"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> duplicate() {
        return ResponseEntity.status(409).body(Map.of("message","A record with these details already exists"));
    }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class) public ResponseEntity<?> conflict() {
        return ResponseEntity.status(409).body(Map.of("message","This order changed. Refresh and try again."));
    }
}
