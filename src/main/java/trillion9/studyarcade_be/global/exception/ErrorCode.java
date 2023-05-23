package trillion9.studyarcade_be.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* 400 BAD_REQUEST */
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "토큰이 유효하지 않습니다"),
    INVALID_USER(HttpStatus.BAD_REQUEST, "작성자만 삭제/수정할 수 있습니다."),
    INVALID_USER_EXISTENCE(HttpStatus.BAD_REQUEST, "중복된 email 입니다."),
    INVALID_USER_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호를 다시 입력해주세요."),
    INVALID_ADMIN_PASSWORD(HttpStatus.BAD_REQUEST, "관리자의 비밀번호를 다시 입력해주세요."),
    INVALID_LIKE(HttpStatus.BAD_REQUEST, "이미 좋아요를 했습니다"),
    INVALID_LIKE_CANCEL(HttpStatus.BAD_REQUEST, "이미 좋아요 취소를 했습니다"),
    INVALID_SIGN(HttpStatus.BAD_REQUEST),
    FILE_SIZE_OVER(HttpStatus.BAD_REQUEST, "파일 용량은 10MB 미만까지 가능합니다."),
    FILE_UNUPLOADED(HttpStatus.BAD_REQUEST, "이미지를 업로드해주세요."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 확장자 입니다."),

    /* 401 UNAUTHORIZED */
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "권한 정보가 없는 토큰입니다"),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "현재 내 계정 정보가 존재하지 않습니다"),

    /* 404 NOT_FOUND */
    POST_TITLE_NOT_FOUND(HttpStatus.NOT_FOUND, "글의 제목이 없습니다."),
    POST_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "글의 내용이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "스터디룸을 찾을 수 없습니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일이 없습니다."),

    /* 409 CONFLICT */
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "데이터가 이미 존재합니다");

    private String message;
    private HttpStatus status;

    ErrorCode(HttpStatus status, String message) {
        this.message = message;
        this.status = status;
    }

    ErrorCode(HttpStatus status) {
        this.status = status;
    }
}
