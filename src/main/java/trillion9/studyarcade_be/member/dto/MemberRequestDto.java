package trillion9.studyarcade_be.member.dto;

import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
public class MemberRequestDto {

    @Pattern(regexp = "^[a-zA-Z가-힣]{2,10}$", message = "닉네임은 2글자~10글자 사이의 알파벳 또는 한글로 입력해주세요.")
    private String nickname;

    @Email
    private String email;
  
    @Pattern(regexp = "^(?=.[0-9])(?=.[a-z])(?=.[A-Z])(?=.\\W)(?=\\S+$).{8,15}$", message = "비밀번호는 최소 8자 이상 15자 이하이며 알파벳 대소문자와 숫자, 특수문자로 구성되어야 합니다.")
    private String password;

    @Pattern(regexp = "^(?=.[0-9])(?=.[a-z])(?=.[A-Z])(?=.\\W)(?=\\S+$).{8,15}$", message = "비밀번호는 최소 8자 이상 15자 이하이며 알파벳 대소문자와 숫자, 특수문자로 구성되어야 합니다.") 

    private String checkPassword;

    @Getter
    public static class login {
        @Email
        private String email;
        private String password;
    }
}