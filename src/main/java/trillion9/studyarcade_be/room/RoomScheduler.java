package trillion9.studyarcade_be.room;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.member.MemberRepository;
import trillion9.studyarcade_be.room.repository.RoomRepository;
import trillion9.studyarcade_be.roommember.RoomMember;
import trillion9.studyarcade_be.roommember.RoomMemberRepository;
import trillion9.studyarcade_be.studytime.StudyTime;
import trillion9.studyarcade_be.studytime.StudyTimeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RoomScheduler {
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final StudyTimeRepository studyTimeRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행되도록 설정
    @Transactional
    public void manageRoomAndStudyTime() {
        LocalDate currentDate = LocalDate.now();
        List<Room> expiredRooms = roomRepository.findAllByExpirationDateBefore(currentDate);

        for (Room room : expiredRooms) {
            List<RoomMember> roomMembers = roomMemberRepository.findAllBySessionId(room.getSessionId());
            if (!roomMembers.isEmpty()) {
                roomMemberRepository.deleteAll(roomMembers);
            }
        }
        roomRepository.deleteAll(expiredRooms);

        // 하루에 공부한 시간 저장 및 통계 저장
        HashOperations<String, String, Long> hash = redisTemplate.opsForHash();

        List<Member> members =  memberRepository.findAll();

        for (Member member : members) {
            // 초 단위로 저장
            Long time = member.getDailyStudyTime();
            // 자정 기준 하루 전 데이터
            LocalDate previousDate = currentDate.minusDays(1);

            StudyTime previousStudyTime = new StudyTime(member.getId(), previousDate, time);
            studyTimeRepository.save(previousStudyTime);

            // dailyStudyTime을 0으로 리셋
            member.setDailyStudyTime(0L);

            LocalDate now = LocalDate.now();

            for (int i = 0; i < 300; i++) {
                StudyTime studyTime = new StudyTime(member.getId(), now.minusDays(i), 123L);
                studyTimeRepository.save(studyTime);
            }

            // 마지막 7일 통계
            List<Object[]> dailyStudyTime = studyTimeRepository.findStudyTimeByDateRange(member.getId(), now.minusDays(6), now.minusDays(1));
            for (int i = 0; i < dailyStudyTime.size(); i++) {
                String day = String.valueOf(dailyStudyTime.get(i)[0].toString());
                Long studyTime = Long.parseLong(dailyStudyTime.get(i)[1].toString());

                hash.put(member.getId() + "D", day, studyTime);
            }
            hash.put(member.getId() + "D", now.toString(), member.getDailyStudyTime());
            redisTemplate.expire(member.getId() + "D", 1, TimeUnit.DAYS);

            // 마지막 7주 통계
            List<Object[]>  weeklyStudyTime = studyTimeRepository.findStudyTimeByWeekRange(member.getId(), now.minusWeeks(6), now);
            for (int i = 0; i < weeklyStudyTime.size(); i++) {
                String week = String.valueOf(weeklyStudyTime.get(i)[0].toString());
                Long studyTime = Long.parseLong(weeklyStudyTime.get(i)[1].toString());
                // 마지막 주에 당일 공부 시간 추가
                if (i == weeklyStudyTime.size() - 1) {
                    studyTime += member.getDailyStudyTime();
                }
                hash.put(member.getId() + "W", week, studyTime);
            }
            redisTemplate.expire(member.getId() + "W", 1, TimeUnit.DAYS);

            // 마지막 7달 통계
            List<Object[]>  monthlyStudyTime = studyTimeRepository.findStudyTimeByMonthRange(member.getId(), now.minusMonths(6), now);
            for (int i = 0; i < monthlyStudyTime.size(); i++) {
                String year = String.valueOf(monthlyStudyTime.get(i)[0].toString());
                String month = String.valueOf(monthlyStudyTime.get(i)[1].toString());
                Long studyTime = Long.parseLong(monthlyStudyTime.get(i)[2].toString());
                // 마지막 달에 당일 공부 시간 추가
                if (i == monthlyStudyTime.size() - 1) {
                    studyTime += member.getDailyStudyTime();
                }
                hash.put(member.getId() + "M", year + "." + month, studyTime);
            }
            redisTemplate.expire(member.getId() + "M", 1, TimeUnit.DAYS);
        }
    }
}
