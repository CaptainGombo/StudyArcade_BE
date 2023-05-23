package trillion9.studyarcade_be.room;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.security.UserDetailsImpl;
import trillion9.studyarcade_be.room.dto.RoomRequestDto;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoomController {

    private final RoomService roomService;

    /* 스터디 룸 생성 */
    @PostMapping("/room/create")
    public ResponseDto<RoomResponseDto> createRoom(@RequestBody RoomRequestDto requestDto,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.createRoom(requestDto, userDetails.getMember());
    }

    /* 스터디 룸 수정 */
    @PatchMapping("/room/{roomId}")
    public ResponseDto<RoomResponseDto> updateRoom(@PathVariable Long roomId,
                             @RequestBody RoomRequestDto requestDto,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.updateRoom(roomId, requestDto, userDetails.getMember());
    }

    /* 스터디 룸 삭제 */
    @DeleteMapping("/room/{roomId}")
    public ResponseDto<RoomResponseDto> deleteRoom(@PathVariable Long roomId,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.deleteRoom(roomId, userDetails.getMember());
    }

    /* 스터디 룸 목록 조회 */
    @GetMapping("/main")
    public ResponseDto<List<RoomResponseDto>> allRooms() {
        return roomService.allRooms();
    }

    /* 스터디 룸 정보 조회 */
    @GetMapping("/room/{roomId}")
    public ResponseDto<RoomResponseDto> infoRoom(@PathVariable Long roomId) {
        return roomService.infoRoom(roomId);
    }
}
