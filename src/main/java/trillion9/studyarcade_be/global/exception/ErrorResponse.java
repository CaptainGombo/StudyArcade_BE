package trillion9.studyarcade_be.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ErrorResponse {
    private final String message;
    private final int statusCode;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode exceptionEnum) {
        return ResponseEntity.status(exceptionEnum.getStatus()).body(ErrorResponse.builder().message(exceptionEnum.getMessage()).statusCode(exceptionEnum.getStatus().value()).build());
    }

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode exceptionEnum, String message) {
        return ResponseEntity.status(exceptionEnum.getStatus()).body(ErrorResponse.builder().message(message).statusCode(exceptionEnum.getStatus().value()).build());
    }
}
