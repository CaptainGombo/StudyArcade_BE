package trillion9.studyarcade_be.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class MemberRequestDto {

    @Pattern(regexp = "^[a-zA-Z가-힣]{2,10}$", message = "닉네임은 2글자~10글자 사이의 알파벳 또는 한글로 입력해주세요.")
    private String nickname;

    @Email
    private String email;

    private String imageUrl;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W)(?=\\S+$).{8,15}$", message = "비밀번호는 최소 8자 이상 15자 이하이며, 알파벳 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W)(?=\\S+$).{8,15}$", message = "비밀번호는 최소 8자 이상 15자 이하이며, 알파벳 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String checkPassword;


    @Getter
    @NoArgsConstructor
    public static class login {
        @Email
        private String email;
        private String password;

        @Builder
        public login(String email, String password) {
            this.email = email;
            this.password = password;
        }

    }

    @Builder
    public MemberRequestDto(String nickname, String email, String imageUrl, String password, String checkPassword) {
        this.nickname = nickname;
        this.email = email;
        this.imageUrl = imageUrl;
        this.password = password;
        this.checkPassword = checkPassword;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}