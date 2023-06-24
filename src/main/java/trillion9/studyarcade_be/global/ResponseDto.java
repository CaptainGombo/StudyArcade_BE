package trillion9.studyarcade_be.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "set")
public class ResponseDto<T> {
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ResponseDto<T> setSuccess(String message, T data) {
        return ResponseDto.set(message, data);
    }

    public static <T> ResponseDto<T> setSuccess(String message) {
        return ResponseDto.set(message, null);
    }
}
