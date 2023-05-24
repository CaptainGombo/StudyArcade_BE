package trillion9.studyarcade_be.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static trillion9.studyarcade_be.global.exception.ErrorCode.FILE_SIZE_OVER;
import static trillion9.studyarcade_be.global.exception.ErrorCode.INVALID_SIGN;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> signValidException(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();

        StringBuilder builder = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("] ");
            builder.append(fieldError.getDefaultMessage());
        }

        return ErrorResponse.toResponseEntity(INVALID_SIGN, builder.toString());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.info("handleMaxUploadSizeExceededException", e);

        return ErrorResponse.toResponseEntity(FILE_SIZE_OVER);
    }

//    @ExceptionHandler(AmazonS3Exception.class)
//    public ResponseEntity<ErrorResponse> handleFileExtensionException(AmazonS3Exception e) {
//        return ErrorResponse.toResponseEntity(INVALID_FILE_EXTENSION);
//    }
}
