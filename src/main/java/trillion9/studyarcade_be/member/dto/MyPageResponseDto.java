package trillion9.studyarcade_be.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Getter
@NoArgsConstructor
public class MyPageResponseDto {
    private String nickname;
    private String email;
    private Long dailyStudyTime;
    private Long totalStudyTime;
    private String title;

    private Map<LocalDate, Long> memberStudyTime;

    @Builder
    private MyPageResponseDto(String nickname, String email, Long dailyStudyTime, Long totalStudyTime, String title, Map<LocalDate, Long> memberStudyTime) {
        this.nickname = nickname;
        this.email = email;
        this.dailyStudyTime = dailyStudyTime;
        this.totalStudyTime = totalStudyTime;
        this.title = title;
        this.memberStudyTime = memberStudyTime;
    }
}
