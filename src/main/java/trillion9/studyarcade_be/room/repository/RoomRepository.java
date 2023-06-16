package trillion9.studyarcade_be.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trillion9.studyarcade_be.room.Room;
import trillion9.studyarcade_be.room.dto.MyRoomResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findBySessionId(String sessionId);
    Optional<Room> findBySessionIdAndIsDelete(String sessionId, boolean isDelete);
    List<Room> findAllByExpirationDateBefore(LocalDate currentDate);
    List<Room> findAllByMemberId(Long memberId);
    @Query("SELECT new trillion9.studyarcade_be.room.dto.MyRoomResponseDto(r.roomName, r.roomContent) FROM Room r WHERE r.memberId = :memberId ORDER BY r.createdAt DESC")
    List<MyRoomResponseDto> findMyRoomResponseDto(@Param("memberId") Long memberId);
}