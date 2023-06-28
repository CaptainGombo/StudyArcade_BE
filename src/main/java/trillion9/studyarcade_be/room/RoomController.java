 package trillion9.studyarcade_be.room;

 import io.openvidu.java.client.OpenViduHttpException;
 import io.openvidu.java.client.OpenViduJavaClientException;
 import lombok.RequiredArgsConstructor;
 import org.springframework.data.domain.Page;
 import org.springframework.http.MediaType;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 import reactor.util.annotation.Nullable;
 import trillion9.studyarcade_be.global.ResponseDto;
 import trillion9.studyarcade_be.global.exception.CustomException;
 import trillion9.studyarcade_be.global.security.UserDetailsImpl;
 import trillion9.studyarcade_be.room.dto.*;

 import java.io.IOException;

 import static trillion9.studyarcade_be.global.exception.ErrorCode.TOKEN_INEXISTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/main")
    public ResponseDto<Page<RoomResponseDto>> allRooms(@RequestParam("page") int page,
                                                       @RequestParam(value = "category", required = false) String category,
                                                       @RequestParam(value = "keyword", required = false) String keyword) {
        return roomService.allRooms(page - 1, category, keyword);
    }

    @PostMapping(value = "/rooms/create", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE, "application/json", "multipart/form-data"})
    public ResponseDto<RoomCreateResponseDto> createRoom(@RequestPart(value = "content") RoomCreateRequestDto requestDto,
                                                         @RequestPart(value = "image", required = false) MultipartFile image,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return roomService.createRoom(requestDto, image, userDetails.getMember());
    }

    @PatchMapping(value = "/rooms/{session-id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE, "application/json", "multipart/form-data"})
    public ResponseDto<String> updateRoom(@PathVariable("session-id") String sessionId,
                                          @RequestPart(value = "content") RoomCreateRequestDto requestDto,
                                          @RequestPart(value = "image", required = false) MultipartFile image,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return roomService.updateRoom(sessionId, requestDto, image, userDetails.getMember());
    }

    @DeleteMapping("/rooms/{session-id}")
    public ResponseDto<String> deleteRoom(@PathVariable("session-id") String sessionId,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) throws OpenViduJavaClientException, OpenViduHttpException {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return roomService.deleteRoom(sessionId, userDetails.getMember());
    }

    @PostMapping("/rooms/{session-id}/enter")
    public ResponseDto<String> enterRoom(@RequestBody @Nullable RoomEnterRequestDto requestDto,
                                         @PathVariable("session-id") String sessionId,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) throws
        OpenViduJavaClientException, OpenViduHttpException {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return roomService.enterRoom(requestDto, sessionId, userDetails.getMember());
    }

    @DeleteMapping("/rooms/{session-id}/out")
    public ResponseDto<String> outRoom(@PathVariable(name = "session-id") String sessionId,
                                       @RequestParam(name = "studytime") Long studyTime,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return roomService.outRoom(sessionId, studyTime, userDetails.getMember());
    }

    @GetMapping(value = "/rooms/{session-id}")
    public ResponseDto<RoomDetailResponseDto> getRoomData(@PathVariable("session-id") String sessionId,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return roomService.getRoomData(sessionId, userDetails.getMember());
    }
}