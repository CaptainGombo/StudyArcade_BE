package trillion9.studyarcade_be.member.dto;

import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
public class MemberRequestDto {

    private String nickname;
    @Email
    private String email;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*?&()_])[A-Za-z\\d$@$!%*?&()_]{8,15}$", message = "비밀번호는 최소 8자 이상 15자 이하이며 알파벳 대소문자와 숫자로 구성되어야 합니다.")
    private String password;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*?&()_])[A-Za-z\\d$@$!%*?&()_]{8,15}$", message = "비밀번호는 최소 8자 이상 15자 이하이며 알파벳 대소문자와 숫자로 구성되어야 합니다.")
    private String checkPassword;

    @Getter
    public static class login {
        @Email
        private String email;
        private String password;
    }
}