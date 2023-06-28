package trillion9.studyarcade_be.room.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

public interface RoomFilter {

    Page<RoomResponseDto> findRooms(Pageable pageable, String category, String keyword);
}
