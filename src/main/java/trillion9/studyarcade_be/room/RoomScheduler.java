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

    @Scheduled(cron = "0 45 * * * *") // 매일 자정에 실행되도록 설정
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

            // 하루에 공부한 시간 저장
            // Redis에 통계 데이터 저장
            HashOperations<String, String, Long> hash = redisTemplate.opsForHash();
            LocalDate now = LocalDate.now();

            // 마지막 7일 통계
            List<Object[]> dailyStudyTime = studyTimeRepository.findStudyTimeByDateRange(member.getId(), now.minusDays(6), now.minusDays(1));
            for (Object[] value : dailyStudyTime) {
                String day = String.valueOf(value[0].toString());
                Long studyTime = Long.parseLong(value[1].toString());

                hash.put(member.getId() + "D", day, studyTime);
            }
            // 0으로 세팅된 당일 통계 데이터 미리 생성
            hash.put(member.getId() + "D", now.toString(), 0L);
            redisTemplate.expire(member.getId() + "D", 1, TimeUnit.DAYS);

            // 마지막 7주 통계
            List<Object[]>  weeklyStudyTime = studyTimeRepository.findStudyTimeByWeekRange(member.getId(), now.minusWeeks(6), now);
            for (Object[] objects : weeklyStudyTime) {
                String week = String.valueOf(objects[0].toString());
                Long studyTime = Long.parseLong(objects[1].toString());

                hash.put(member.getId() + "W", week, studyTime);
            }
            redisTemplate.expire(member.getId() + "W", 1, TimeUnit.DAYS);

            // 마지막 7달 통계
            List<Object[]>  monthlyStudyTime = studyTimeRepository.findStudyTimeByMonthRange(member.getId(), now.minusMonths(6), now);
            for (Object[] objects : monthlyStudyTime) {
                String year = String.valueOf(objects[0].toString());
                String month = String.valueOf(objects[1].toString());
                Long studyTime = Long.parseLong(objects[2].toString());

                hash.put(member.getId() + "M", year + "." + month, studyTime);
            }
            redisTemplate.expire(member.getId() + "M", 1, TimeUnit.DAYS);
        }
    }
}
