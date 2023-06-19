package trillion9.studyarcade_be.global.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static trillion9.studyarcade_be.global.exception.ErrorCode.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        Sentry.captureException(e);
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> uncheckedError(Exception e) {
        Sentry.captureException(e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> signValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        StringBuilder builder = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("] ");
            builder.append(fieldError.getDefaultMessage());
        }
        Sentry.captureException(e);
        return ErrorResponse.toResponseEntity(INVALID_SIGN, builder.toString());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> duplicateDataException(DataIntegrityViolationException e) {
        Sentry.captureException(new CustomException(DUPLICATE_DATA));
        return ErrorResponse.toResponseEntity(DUPLICATE_DATA);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        Sentry.captureException(new CustomException(FILE_SIZE_OVER));
        return ErrorResponse.toResponseEntity(FILE_SIZE_OVER);
    }

    @ExceptionHandler(AmazonS3Exception.class)
    public ResponseEntity<ErrorResponse> handleFileExtensionException(AmazonS3Exception e) {
        Sentry.captureException(new CustomException(INVALID_FILE_EXTENSION));
        return ErrorResponse.toResponseEntity(INVALID_FILE_EXTENSION);
    }
}
