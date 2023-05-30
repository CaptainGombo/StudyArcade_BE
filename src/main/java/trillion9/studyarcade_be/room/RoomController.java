package trillion9.studyarcade_be.room;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.security.UserDetailsImpl;
import trillion9.studyarcade_be.room.dto.RoomCreateRequestDto;
import trillion9.studyarcade_be.room.dto.RoomCreateResponseDto;
import trillion9.studyarcade_be.room.dto.RoomDetailResponseDto;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

import java.io.IOException;
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
    public ResponseDto<RoomCreateResponseDto> createRoom(@RequestPart(value = "content") RoomCreateRequestDto requestDto,
                                                   @RequestParam(value = "image", required = false) MultipartFile image,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {
        return roomService.createRoom(requestDto, image, userDetails.getMember());
    }

    /* 스터디 룸 수정 */
    @Operation(summary = "스터디 룸 수정 API", description = "스터디 룸 수정")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 수정 완료")})
    @PatchMapping("/room/{session-id}")
    public ResponseDto<RoomDetailResponseDto> updateRoom(@PathVariable("session-id") Long sessionId,
                                                   @RequestBody RoomCreateRequestDto requestDto,
                                                   @RequestParam(value = "image", required = false) MultipartFile image,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return roomService.updateRoom(roomId, requestDto, image, userDetails.getMember());
    }

    /* 스터디 룸 삭제 */
    @Operation(summary = "스터디 룸 삭제 API", description = "스터디 룸 삭제")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 삭제 완료")})
    @DeleteMapping("/room/{session-id}")
    public ResponseDto<RoomDetailResponseDto> deleteRoom(@PathVariable("session-id") Long sessionId,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.deleteRoom(roomId, userDetails.getMember());
    }

    /* 스터디 룸 입장 */
    @Operation(summary = "스터디 룸 입장 API", description = "스터디 룸 입장")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 입장 완료")})
    @PostMapping("/room/{session-id}/enter")
    public String enterRoom(@PathVariable("session-id") String sessionId,
                            @AuthenticationPrincipal UserDetailsImpl userDetails) throws
        OpenViduJavaClientException, OpenViduHttpException {
        return roomService.enterRoom(sessionId, userDetails.getMember());
    }

    /* 스터디 룸 퇴장 */
    @Operation(summary = "스터디 룸 퇴장 API", description = "스터디 룸 퇴장")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 퇴장 완료")})
    @DeleteMapping("/room/{session-id}/out")
    public String outRoom(@PathVariable(name = "session-id") String sessionId,
                          @AuthenticationPrincipal UserDetailsImpl userDetails, Duration roomStudyTime) {
        return roomService.outRoom(sessionId, userDetails.getMember(), roomStudyTime);
    }
}
