package trillion9.studyarcade_be.room.repository;

import org.springframework.data.domain.Page;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

import org.springframework.data.domain.Pageable;

public interface RoomFilter {
    Page<RoomResponseDto> findAllRoom(Pageable pageable);
}
