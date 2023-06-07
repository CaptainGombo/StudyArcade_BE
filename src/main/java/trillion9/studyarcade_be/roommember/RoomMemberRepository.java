package trillion9.studyarcade_be.roommember;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    Optional<RoomMember> findByMemberIdAndSessionId(Long memberId, String sessionId);
    Optional<RoomMember> findByMemberIdAndSessionIdAndRoomMaster(Long memberId, String sessionId, boolean roomMaster);
    List<RoomMember> findBySessionId(String sessionId);

    List<RoomMember> findAllBySessionId(String sessionId);
}
