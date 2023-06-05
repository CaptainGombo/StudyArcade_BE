package trillion9.studyarcade_be.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trillion9.studyarcade_be.room.Room;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findBySessionId(String sessionId);

    Optional<Room> findBySessionIdAndIsDelete(String sessionId, boolean isDelete);
}