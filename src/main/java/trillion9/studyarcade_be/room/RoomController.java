package trillion9.studyarcade_be.room;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.security.UserDetailsImpl;
import trillion9.studyarcade_be.room.dto.RoomRequestDto;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

import java.util.List;
@Tag(name = "RoomController",description = "스터디룸 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoomController {

    private final RoomService roomService;

    /* 스터디 룸 생성 */
    @Operation(summary = "스터디 룸 생성 API", description = "스터디 룸 생성")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 생성 완료")})
    @PostMapping("/room/create")
    public ResponseDto<RoomResponseDto> createRoom(@RequestBody RoomRequestDto requestDto,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.createRoom(requestDto, userDetails.getMember());
    }

    /* 스터디 룸 수정 */
    @Operation(summary = "스터디 룸 수정 API", description = "스터디 룸 수정")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 수정 완료")})
    @PatchMapping("/room/{roomId}")
    public ResponseDto<RoomResponseDto> updateRoom(@PathVariable Long roomId,
                             @RequestBody RoomRequestDto requestDto,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.updateRoom(roomId, requestDto, userDetails.getMember());
    }

    /* 스터디 룸 삭제 */
    @Operation(summary = "스터디 룸 삭제 API", description = "스터디 룸 삭제")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 삭제 완료")})
    @DeleteMapping("/room/{roomId}")
    public ResponseDto<RoomResponseDto> deleteRoom(@PathVariable Long roomId,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.deleteRoom(roomId, userDetails.getMember());
    }

    /* 스터디 룸 목록 조회 */
    @Operation(summary = "스터디 룸 목록 조회 API", description = "스터디 룸 목록 조회")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 목록 조회 완료")})
    @GetMapping("/main")
    public ResponseDto<List<RoomResponseDto>> allRooms() {
        return roomService.allRooms();
    }

    /* 스터디 룸 정보 조회 */
    @Operation(summary = "스터디 룸 정보 조회 API", description = "스터디 룸 정보 조회")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 정보 조회 완료")})
    @GetMapping("/room/{roomId}")
    public ResponseDto<RoomResponseDto> infoRoom(@PathVariable Long roomId) {
        return roomService.infoRoom(roomId);
    }
}
