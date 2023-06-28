package trillion9.studyarcade_be.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import trillion9.studyarcade_be.room.Room;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findBySessionId(String sessionId);
    Optional<Room> findBySessionIdAndMemberId(String sessionId, Long memberId);
    List<Room> findAllByExpirationDateBefore(LocalDate currentDate);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Long countAllByMemberId(Long memberId);

    List<RoomResponseDto> findAllByMemberId(Long MemberId);
}