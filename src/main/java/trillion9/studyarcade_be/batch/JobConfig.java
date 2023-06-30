package trillion9.studyarcade_be.batch;

import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.room.Room;
import trillion9.studyarcade_be.room.repository.RoomRepository;
import trillion9.studyarcade_be.roommember.RoomMember;
import trillion9.studyarcade_be.roommember.RoomMemberRepository;
import trillion9.studyarcade_be.studytime.StudyTime;
import trillion9.studyarcade_be.studytime.StudyTimeRepository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final RoomRepository roomRepository;
    private final StudyTimeRepository studyTimeRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;
    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openvidu;


    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    private final int CHUNK_SIZE = 10;

    // Batch Job의 Step 설정
    // roomStep -> memberStep -> sessionStep
    @Bean
    public Job roomAndStudyTimeJob(Step roomStep, Step sessionStep, Step memberStep) {
        return jobBuilderFactory.get("roomAndStudyTimeJob")
//                .incrementer(new RunIdIncrementer()) // Incrementer를 통해 JobParameter 구분
                .start(roomStep)
                .next(sessionStep)
                .next(memberStep)
                .build();
    }

    // 만료일이 된 Room 삭제 및 해당 Room에 포함된 RoomMember 삭제 Step
    @Bean
    public Step roomStep() {
        return stepBuilderFactory.get("roomStep")
                .tasklet(roomTasklet())
                .build();
    }

    // 현재 RoomMember가 존재하는 세션 외의 모든 세션 삭제 Step
    @Bean
    public Step sessionStep() {
        return stepBuilderFactory.get("sessionStep")
                .tasklet(sessionTasklet())
                .build();
    }

    // 모든 멤버의 전날 공부 시간 저장 및 통계 데이터 계산 Step
    @Bean
    public Step memberStep() {
        return stepBuilderFactory.get("studyTimeStep")
                .<Member, Member> chunk(CHUNK_SIZE)
                .reader(memberReader())
                .processor(updateDailyStudyTime())
                .writer(updateStudyTimeStatistics())
                .build();
    }

    public Tasklet roomTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Start Tasklet");
            LocalDate currentDate = LocalDate.now();
            List<Room> expiredRooms = roomRepository.findAllByExpirationDateBefore(currentDate);

            for (Room room : expiredRooms) {
                List<RoomMember> roomMembers = roomMemberRepository.findAllBySessionId(room.getSessionId());
                if (!roomMembers.isEmpty()) {
                    roomMemberRepository.deleteAll(roomMembers);
                }
            }
            roomRepository.deleteAll(expiredRooms);
            return RepeatStatus.FINISHED;
        };
    }

    public Tasklet sessionTasklet() {
        return (contribution, chunkContext) -> {
            List<String> sessionIds = roomMemberRepository.findActiveSessionIds();

            openvidu.fetch();
            List<Session> activeSessionList = openvidu.getActiveSessions();

            activeSessionList.removeIf(session -> sessionIds.contains(session.getSessionId()));

            for (Session session : activeSessionList) {
                session.close();
            }
            return RepeatStatus.FINISHED;
        };
    }

    public JpaPagingItemReader<Member> memberReader() {
        return new JpaPagingItemReaderBuilder<Member>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT m FROM Member m")
                .build();
    }

    public ItemProcessor<Member, Member> updateDailyStudyTime() {
        return member -> {
            // 자정 기준 하루 전 데이터
            LocalDate previousDate = LocalDate.now().minusDays(1);

            StudyTime previousStudyTime = new StudyTime(member.getId(), previousDate, member.getDailyStudyTime());
            studyTimeRepository.save(previousStudyTime);

            // dailyStudyTime을 0으로 리셋
            member.setDailyStudyTime(0L);
            return member;
        };
    }

    // Redis에 통계 데이터 저장
    public ItemWriter<Member> updateStudyTimeStatistics() {
        HashOperations<String, String, Long> hash = redisTemplate.opsForHash();
        LocalDate now = LocalDate.now();

        return members -> {
            for (Member member : members) {
                // 마지막 7일 통계
                String dailyKey = member.getId() + "D";
                // 기존 데이터 삭제
                if (!hash.keys(dailyKey).isEmpty()) hash.delete(dailyKey);

                List<Object[]> dailyStudyTime = studyTimeRepository.findStudyTimeByDateRange(member.getId(), now.minusDays(6), now.minusDays(1));
                for (Object[] value : dailyStudyTime) {
                    String day = String.valueOf(value[0].toString());
                    Long studyTime = Long.parseLong(value[1].toString());
                    if (day != null && studyTime != null) { // 값이 null이 아닌 경우에만 저장
                        hash.put(dailyKey, day, studyTime); // 새로운 데이터 저장
                    }
                }
                hash.put(dailyKey, now.toString(), 0L);
                redisTemplate.expire(dailyKey, 1, TimeUnit.DAYS);

                // 마지막 7주 통계
                String weeklyKey = member.getId() + "W";
                // 기존 데이터 삭제
                if (!hash.keys(weeklyKey).isEmpty()) hash.delete(weeklyKey);

                List<Object[]> weeklyStudyTime = studyTimeRepository.findStudyTimeByWeekRange(member.getId(), now.minusWeeks(6), now);
                for (Object[] objects : weeklyStudyTime) {
                    String week = String.valueOf(objects[0].toString());
                    Long studyTime = Long.parseLong(objects[1].toString());
                    if (week != null && studyTime != null) { // 값이 null이 아닌 경우에만 저장
                        hash.put(weeklyKey, week, studyTime); // 새로운 데이터 저장
                    }
                }
                redisTemplate.expire(weeklyKey, 1, TimeUnit.DAYS);

                // 마지막 7달 통계
                String monthlyKey = member.getId() + "M";
                // 기존 데이터 삭제
                if (!hash.keys(monthlyKey).isEmpty()) hash.delete(monthlyKey);
                List<Object[]> monthlyStudyTime = studyTimeRepository.findStudyTimeByMonthRange(member.getId(), now.minusMonths(6), now);
                for (Object[] objects : monthlyStudyTime) {
                    String year = String.valueOf(objects[0].toString());
                    String month = String.valueOf(objects[1].toString());
                    Long studyTime = Long.parseLong(objects[2].toString());
                    if (year != null && month != null && studyTime != null) { // 값이 null이 아닌 경우에만 저장
                        hash.put(monthlyKey, year + "." + month, studyTime); // 새로운 데이터 저장
                    }
                }
                redisTemplate.expire(monthlyKey, 1, TimeUnit.DAYS);
            }
        };
    }

    /* Session을 Chunk로 처리할 경우 */

    //    @Bean
//    public Step sessionStep() throws OpenViduJavaClientException, OpenViduHttpException {
//        return stepBuilderFactory.get("sessionStep")
//                .<String, String> chunk(CHUNK_SIZE)
//                .reader(sessionReader())
//                .writer(sessionWriter())
//                .build();
//    }

//    public JpaPagingItemReader<String> sessionReader() {
//        return new JpaPagingItemReaderBuilder<String>()
//                .name("jpaPagingItemReader")
//                .entityManagerFactory(entityManagerFactory)
//                .pageSize(CHUNK_SIZE)
//                .queryString("SELECT DISTINCT m.sessionId FROM RoomMember m")
//                .build();
//    }

    // 서버 Session과 비교해야 하기 때문에 Cursor 사용
//    public JpaCursorItemReader<String> sessionReader() {
//        return new JpaCursorItemReaderBuilder<String>()
//                .name("jpaCursorItemReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT DISTINCT m.sessionId FROM RoomMember m")
//                .build();
//    }

//    public ItemWriter<String> sessionWriter() throws OpenViduJavaClientException, OpenViduHttpException {
//
//            openvidu.fetch();
//            List<Session> activeSessionList = openvidu.getActiveSessions();
//
//            activeSessionList.removeIf(session -> sessionIds.contains(session.getSessionId()));
//
//            for (Session session : activeSessionList) {
//                try {
//                    session.close();
//                    log.info("Session " + session.getSessionId() + " closed");
//                } catch (OpenViduHttpException e) {
//                    // 세션을 닫을 때 발생한 예외 처리
//                    log.error("Failed to close session " + session.getSessionId(), e);
//                }
//            }
//        };
//    }
}
