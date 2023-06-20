package trillion9.studyarcade_be.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopRankedResponseDto {
    private String nickname;
    private String title;
    private Long totalStudyTime;

    public TopRankedResponseDto(String nickname, String title, Long totalStudyTime) {
        this.nickname = nickname;
        this.title = title;
        this.totalStudyTime = totalStudyTime;
    }
}
