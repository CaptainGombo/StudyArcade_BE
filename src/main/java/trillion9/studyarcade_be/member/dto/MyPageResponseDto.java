package trillion9.studyarcade_be.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

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
    private Long nextGradeRemainingTime;
    private String topRankedNickname;
    private String topRankedTitle;
    private Long topRankedTotalStudyTime;
    private List<TopRankedResponseDto> topRankedList;
    private List<RoomResponseDto> myRooms;

    private Map<String, Long> dailyStudyChart;
    private Map<String, Long> weeklyStudyChart;
    private Map<String, Long> monthlyStudyChart;

    @Builder
    private MyPageResponseDto(String nickname, String email, Long dailyStudyTime, Long totalStudyTime, String title, Long nextGradeRemainingTime,
                              String topRankedNickname, String topRankedTitle, Long topRankedTotalStudyTime,
                              List<TopRankedResponseDto> topRankedList,
                              List<RoomResponseDto> myRooms,
                              Map<String, Long> dailyStudyChart,
                              Map<String, Long> weeklyStudyChart,
                              Map<String, Long> monthlyStudyChart) {

        this.nickname = nickname;
        this.email = email;
        this.dailyStudyTime = dailyStudyTime;
        this.totalStudyTime = totalStudyTime;
        this.title = title;
        this.nextGradeRemainingTime = nextGradeRemainingTime;
        this.topRankedNickname = topRankedNickname;
        this.topRankedTitle = topRankedTitle;
        this.topRankedTotalStudyTime = topRankedTotalStudyTime;
        this.topRankedList = topRankedList;
        this.myRooms = myRooms;
        this.dailyStudyChart = dailyStudyChart;
        this.weeklyStudyChart = weeklyStudyChart;
        this.monthlyStudyChart = monthlyStudyChart;
    }
}
