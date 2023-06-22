package trillion9.studyarcade_be.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopRankedResponseDto {
    private int rank;
    private String nickname;
    private String title;
    private Long totalStudyTime;

    public TopRankedResponseDto(int rank, String nickname, String title, Long totalStudyTime) {
        this.rank = rank;
        this.nickname = nickname;
        this.title = title;
        this.totalStudyTime = totalStudyTime;
    }
}