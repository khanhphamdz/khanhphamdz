package com.datn.teeshirt.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation file upload
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseObject> handleIOException(IOException e) {
        return ResponseEntity.ok(new ResponseObject("error", e.getMessage(), null));
    }

    /**
     * Xử lý lỗi file quá lớn
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseObject> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ResponseEntity.ok(new ResponseObject("error", "File quá lớn. Kích thước tối đa: 5MB", null));
    }

    /**
     * Xử lý lỗi runtime (validation, business logic)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseObject> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.ok(new ResponseObject("error", e.getMessage(), null));
    }

    /**
     * Xử lý lỗi không xác định
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject> handleGenericException(Exception e) {
        // Log error chi tiết cho developer
        e.printStackTrace();

        // Trả về message đơn giản cho user
        return ResponseEntity.ok(new ResponseObject("error", "Có lỗi xảy ra. Vui lòng thử lại sau.", e.getMessage()));
    }

    /**
     * Xử lý lỗi validation (Bean Validation)
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseObject> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.ok(new ResponseObject("error", "Dữ liệu không hợp lệ", errors));
    }
}