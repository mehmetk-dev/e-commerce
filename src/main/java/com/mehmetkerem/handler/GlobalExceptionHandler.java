package com.mehmetkerem.handler;

import com.mehmetkerem.exception.BaseException;
import com.mehmetkerem.util.Result;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@SuppressWarnings("null")
@lombok.extern.slf4j.Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Result> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {
        return new ResponseEntity<>(ResultHelper.error("Hatalı e-posta veya şifre.", HttpStatus.UNAUTHORIZED),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Result> handleBaseException(BaseException ex) {
        return new ResponseEntity<>(ResultHelper.error(ex.getMessage(), ex.getStatus()), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultData<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return new ResponseEntity<>(ResultHelper.validateError(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(ResultHelper.error(
                "Dosya boyutu çok büyük. Maksimum 5MB yüklenebilir.", HttpStatus.PAYLOAD_TOO_LARGE),
                HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Result> handleOptimisticLockingFailure(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        return new ResponseEntity<>(ResultHelper.error(
                "Veri başka bir işlem tarafından güncellendi. Lütfen sayfayı yenileyip tekrar deneyin.",
                HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleGeneralException(Exception ex) {
        log.error("Beklenmeyen hata oluştu: ", ex);
        return new ResponseEntity<>(ResultHelper.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}