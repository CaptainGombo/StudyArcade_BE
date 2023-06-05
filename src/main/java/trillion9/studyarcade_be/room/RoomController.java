 package trillion9.studyarcade_be.room;

 import io.openvidu.java.client.OpenViduHttpException;
 import io.openvidu.java.client.OpenViduJavaClientException;
 import io.swagger.v3.oas.annotations.Operation;
 import io.swagger.v3.oas.annotations.responses.ApiResponse;
 import io.swagger.v3.oas.annotations.responses.ApiResponses;
 import io.swagger.v3.oas.annotations.tags.Tag;
 import lombok.RequiredArgsConstructor;
 import org.springframework.data.domain.Page;
 import org.springframework.http.MediaType;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 import trillion9.studyarcade_be.global.ResponseDto;
 import trillion9.studyarcade_be.global.security.UserDetailsImpl;
 import trillion9.studyarcade_be.room.dto.*;

 import java.io.IOException;
@Tag(name = "RoomController",description = "스터디룸 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoomController {

    private final RoomService roomService;

    /* 스터디 룸 목록 조회 */
    @Operation(summary = "스터디 룸 목록 조회 API", description = "스터디 룸 목록 조회")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 목록 조회 완료")})
    @GetMapping("/main")
    public ResponseDto<Page<RoomResponseDto>> allRooms(@RequestParam("page") int page) {
        return roomService.allRooms(page - 1);
    }

    /* 스터디 룸 생성 */
    @Operation(summary = "스터디 룸 생성 API", description = "스터디 룸 생성")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 생성 완료")})
    @PostMapping(value = "/rooms/create", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE, "application/json", "multipart/form-data"})
    public ResponseDto<RoomCreateResponseDto> createRoom(@RequestPart(value = "content") RoomCreateRequestDto requestDto,
                                                         @RequestPart(value = "image", required = false) MultipartFile image,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {
        return roomService.createRoom(requestDto, image, userDetails.getMember());
    }

    /* 스터디 룸 수정 */
    @Operation(summary = "스터디 룸 수정 API", description = "스터디 룸 수정")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 수정 완료")})
    @PatchMapping(value = "/rooms/{session-id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE, "application/json", "multipart/form-data"})
    public ResponseDto<RoomDetailResponseDto> updateRoom(@PathVariable("session-id") String sessionId,
                                                         @RequestPart(value = "content") RoomCreateRequestDto requestDto,
                                                         @RequestPart(value = "image", required = false) MultipartFile image,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return roomService.updateRoom(sessionId, requestDto, image, userDetails.getMember());
    }

    /* 스터디 룸 삭제 */
    @Operation(summary = "스터디 룸 삭제 API", description = "스터디 룸 삭제")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 삭제 완료")})
    @DeleteMapping("/rooms/{session-id}")
    public ResponseDto<RoomDetailResponseDto> deleteRoom(@PathVariable("session-id") String sessionId,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.deleteRoom(sessionId, userDetails.getMember());
    }

    /* 스터디 룸 입장 */
    @Operation(summary = "스터디 룸 입장 API", description = "스터디 룸 입장")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 입장 완료")})
    @PostMapping("/rooms/{session-id}/enter")
    public String enterRoom(@RequestBody @Nullable RoomEnterRequestDto requestDto,
                            @PathVariable("session-id") String sessionId,
                            @AuthenticationPrincipal UserDetailsImpl userDetails) throws
        OpenViduJavaClientException, OpenViduHttpException {
        return roomService.enterRoom(requestDto, sessionId, userDetails.getMember());
    }

    /* 스터디 룸 퇴장 */
    @Operation(summary = "스터디 룸 퇴장 API", description = "스터디 룸 퇴장")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "스터디 룸 퇴장 완료")})
    @DeleteMapping("/rooms/{session-id}/out")
    public String outRoom(@PathVariable(name = "session-id") String sessionId,
                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return roomService.outRoom(sessionId, userDetails.getMember());
    }
}