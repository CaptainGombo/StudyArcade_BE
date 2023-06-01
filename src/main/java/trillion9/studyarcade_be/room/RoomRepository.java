package trillion9.studyarcade_be.room;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findBySessionId(Long sessionId);

    List<Room> findAllByOrderByCreatedAtDesc();


	Optional<Room> findBySessionIdAndIsDelete(Long sessionId, boolean isDelete);

	Page<Room> findAll(Pageable pageable);
}
