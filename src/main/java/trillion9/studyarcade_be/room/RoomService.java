package trillion9.studyarcade_be.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.room.dto.RoomRequestDto;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

import java.util.List;
import java.util.stream.Collectors;

import static trillion9.studyarcade_be.global.exception.ErrorCode.INVALID_USER;
import static trillion9.studyarcade_be.global.exception.ErrorCode.ROOM_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    /* 스터디 룸 생성 */
    @Transactional
    public ResponseDto<RoomResponseDto> createRoom(RoomRequestDto requestDto, Member member) {
        Room room = new Room(requestDto, member);
        roomRepository.save(room);
        return ResponseDto.setSuccess("스터디 룸 생성 성공", new RoomResponseDto(room));
    }

    /* 스터디 룸 수정 */
    @Transactional
    public ResponseDto<RoomResponseDto> updateRoom(Long roomId, RoomRequestDto requestDto, Member member) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );
        if (!room.getMember().getId().equals(member.getId())) {
            throw new CustomException(INVALID_USER);
        }
        room.updateRoom(requestDto);
        roomRepository.save(room);
        return ResponseDto.setSuccess("스터디 룸 수정 성공", new RoomResponseDto(room));
    }

    /* 스터디 룸 삭제 */
    @Transactional
    public ResponseDto<RoomResponseDto> deleteRoom(Long roomId, Member member) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );
        if (!room.getMember().getId().equals(member.getId())) {
            throw new CustomException(INVALID_USER);
        }
        roomRepository.delete(room);
        return ResponseDto.setSuccess("스터디 룸 삭제 성공");
    }

    /* 스터디 룸 목록 조회 */
    @Transactional(readOnly = true)
    public ResponseDto<List<RoomResponseDto>> allRooms() {
        List<Room> room = roomRepository.findAllByOrderByCreatedAtDesc();
        List<RoomResponseDto> roomResponseDto = room.stream()
                .map(RoomResponseDto::new)
                .collect(Collectors.toList());
        return ResponseDto.setSuccess("스터디 룸 목록 조회 성공", roomResponseDto);
    }

    /* 스터디 룸 정보 조회 */
    @Transactional(readOnly = true)
    public ResponseDto<RoomResponseDto> infoRoom(Long roomId) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );
        return ResponseDto.setSuccess("스터디 룸 정보 조회 성공", new RoomResponseDto(room));
    }
}
