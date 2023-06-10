package trillion9.studyarcade_be.room;

import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class RoomScheduler {
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final StudyTimeRepository studyTimeRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Autowired
    public RoomScheduler(RoomRepository roomRepository, MemberRepository memberRepository, StudyTimeRepository studyTimeRepository, RoomMemberRepository roomMemberRepository) {
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.studyTimeRepository = studyTimeRepository;
        this.roomMemberRepository = roomMemberRepository;
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행되도록 설정
    @Transactional
    public void manageRoomAndStudyTime() {
        LocalDate currentDate = LocalDate.now();
        List<Room> expiredRooms = roomRepository.findByExpirationDateBefore(currentDate);

        for (Room room : expiredRooms) {
            List<RoomMember> roomMembers = roomMemberRepository.findBySessionId(room.getSessionId());
            if (!roomMembers.isEmpty()) {
                roomMemberRepository.deleteAll(roomMembers);
            }
        }

        roomRepository.deleteAll(expiredRooms);

        // 하루에 공부한 시간 저장
        List<Member> members =  memberRepository.findAll();

        for (Member member : members) {
            // 분 단위로 저장
            Long minute = member.getDailyStudyTime()  / 60;
            // 자정 기준 하루 전 데이터
            LocalDate previousDate = currentDate.minusDays(1);

            StudyTime dailyStudyTime = new StudyTime(member.getId(), previousDate, minute);
            studyTimeRepository.save(dailyStudyTime);

            // 멤버의 dailyStudyTime을 totalStudyTime에 더하고 0으로 리셋
            member.updateTotalStudyTime();
        }
    }
}
