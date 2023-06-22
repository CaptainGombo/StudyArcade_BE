package trillion9.studyarcade_be.roommember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    Optional<RoomMember> findByMemberIdAndSessionId(Long memberId, String sessionId);
    Optional<RoomMember> findByMemberIdAndSessionIdAndRoomMaster(Long memberId, String sessionId, boolean roomMaster);
    List<RoomMember> findAllBySessionId(String sessionId);
    Optional<RoomMember> findByMemberId(Long memberId);

    @Query("SELECT DISTINCT m.sessionId FROM RoomMember m")
    List<String> findActiveSessionIds();
}
