package trillion9.studyarcade_be.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class MemberResponseDto {
    private String nickname;
    private String email;
    private String imageUrl;

    @Builder
    private MemberResponseDto(String nickname, String email, String imageUrl) {
        this.nickname = nickname;
        this.email = email;
        this.imageUrl = imageUrl;
    }
}
