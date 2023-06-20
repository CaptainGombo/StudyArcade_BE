package trillion9.studyarcade_be.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResponseDto {
    private String nickname;
    private String email;
    private String imageUrl;
    private String title;
    private Long nextGradeRemainingTime;

    @Builder
    private MemberResponseDto(String nickname, String email, String imageUrl, String title, Long nextGradeRemainingTime) {
        this.nickname = nickname;
        this.email = email;
        this.imageUrl = imageUrl;
        this.title = title;
        this.nextGradeRemainingTime = nextGradeRemainingTime;
    }
}
