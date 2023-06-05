package trillion9.studyarcade_be.roommember;

import org.springframework.data.jpa.repository.JpaRepository;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.room.Room;

import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    Optional<RoomMember> findByMemberIdAndSessionIdAndIsOut(Long memberId, String sessionId, boolean isOut);
    Optional<RoomMember> findByMemberIdAndSessionIdAndRoomMaster(Long memberId, String sessionId, boolean roomMaster);

}
