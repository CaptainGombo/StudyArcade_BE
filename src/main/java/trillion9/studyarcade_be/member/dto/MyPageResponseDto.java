package trillion9.studyarcade_be.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.Room;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class MyPageResponseDto {
    private String nickname;
    private String email;
    private Long dailyStudyTime;
    private Long totalStudyTime;
    private String title;
    private String topRanked;

    private Map<String, Long> dailyStudyChart;
    private Map<String, Long> weeklyStudyChart;
    private Map<String, Long> monthlyStudyChart;

    private List<Room> myRooms;

    @Builder
    private MyPageResponseDto(String nickname, String email, Long dailyStudyTime, Long totalStudyTime, String title, String topRanked,
                              Map<String, Long> dailyStudyChart,
                              Map<String, Long> weeklyStudyChart,
                              Map<String, Long> monthlyStudyChart,
                              List<Room> myRooms) {

        this.nickname = nickname;
        this.email = email;
        this.dailyStudyTime = dailyStudyTime;
        this.totalStudyTime = totalStudyTime;
        this.title = title;
        this.topRanked = topRanked;
        this.dailyStudyChart = dailyStudyChart;
        this.weeklyStudyChart = weeklyStudyChart;
        this.monthlyStudyChart = monthlyStudyChart;
        this.myRooms = myRooms;
    }
}
