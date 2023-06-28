package trillion9.studyarcade_be.roommember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    Optional<RoomMember> findByMemberId(Long memberId);
    Optional<RoomMember> findByMemberIdAndSessionId(Long memberId, String sessionId);
    List<RoomMember> findAllBySessionId(String sessionId);

    @Query("SELECT DISTINCT m.sessionId FROM RoomMember m")
    List<String> findActiveSessionIds();
}
