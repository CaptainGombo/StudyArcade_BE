package trillion9.studyarcade_be.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /* 400 BAD_REQUEST */
    INVALID_SIGN(HttpStatus.BAD_REQUEST),
    INVALID_USER(HttpStatus.BAD_REQUEST, "작성자만 삭제/수정할 수 있습니다."),
    INVALID_USER_EXISTENCE(HttpStatus.BAD_REQUEST, "중복된 email 입니다."),
    INVALID_USER_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호를 다시 입력해주세요."),
    INVALID_ADMIN_PASSWORD(HttpStatus.BAD_REQUEST, "관리자의 비밀번호를 다시 입력해주세요."),
    FILE_SIZE_OVER(HttpStatus.BAD_REQUEST, "파일 용량은 10MB 미만까지 가능합니다."),
    FILE_UNUPLOADED(HttpStatus.BAD_REQUEST, "이미지를 업로드해주세요."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 확장자 입니다."),
    INVALID_ROOM_COUNT(HttpStatus.BAD_REQUEST, "방의 개수가 3개를 초과하였습니다."),
    INVALID_PASSWORD_INPUT(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요."),
    INVALID_PASSWORD_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ROOM_FULL(HttpStatus.BAD_REQUEST, "방이 가득찼습니다."),

    /* 401 UNAUTHORIZED */
    AUTHORIZATION_ERROR(HttpStatus.UNAUTHORIZED, "유효하지 않은 접근 방식입니다."),

    /* 403 FORBIDDEN */
    TOKEN_INEXISTENT(HttpStatus.FORBIDDEN, "토큰이 존재하지 않습니다."),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, "Invalid Token"),
    INVALID_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "Refresh Token Expired"),

    /* 404 NOT_FOUND */
    POST_TITLE_NOT_FOUND(HttpStatus.NOT_FOUND, "글의 제목이 없습니다."),
    POST_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "글의 내용이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "스터디룸을 찾을 수 없습니다"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일이 없습니다."),
    ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "방에 존재하는 유저가 아닙니다."),

    /* 409 CONFLICT */
    MEMBER_ALREADY_ENTERED(HttpStatus.CONFLICT, "이미 입장한 멤버입니다."),
    ROOM_MEMBER_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "하나의 방에만 입장할 수 있습니다"),
    DUPLICATE_DATA(HttpStatus.CONFLICT, "중복된 데이터가 존재합니다");

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
