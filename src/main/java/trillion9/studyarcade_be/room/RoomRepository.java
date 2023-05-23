package trillion9.studyarcade_be.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByOrderByCreatedAtDesc();
    Optional<Room> findByRoomId(Long roomId);
}
