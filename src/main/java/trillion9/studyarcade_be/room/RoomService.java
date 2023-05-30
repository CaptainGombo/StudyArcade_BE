package trillion9.studyarcade_be.room;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.room.dto.RoomRequestDto;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;
import trillion9.studyarcade_be.roommember.RoomMember;
import trillion9.studyarcade_be.roommember.RoomMemberRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;

import static trillion9.studyarcade_be.global.exception.ErrorCode.INVALID_USER;
import static trillion9.studyarcade_be.global.exception.ErrorCode.ROOM_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final AmazonS3 amazonS3;

    /* 스터디 룸 생성 */
    @Transactional
    public ResponseDto<RoomCreateResponseDto> createRoom(RoomCreateRequestDto requestDto, MultipartFile image, Member member)
        throws Exception {

        /* Session Id 셋팅 */
        RoomCreateResponseDto newToken = createNewSession(member);

        log.info("user 정보 : " + member.getEmail());
        log.info("user 정보 : " + member.getNickname());

        String imageUrl = (image != null || !image.isEmpty()) ? uploadImage(image) : "대표 이미지 URL";

        Room room = Room.builder()
                .roomName(requestDto.getRoomName())
                .roomContent(requestDto.getRoomContent())
                .imageUrl(imageUrl)
                .build();

        RoomMember roomMember = new RoomMember();
        roomMember.setRoomMaster(true, member);

        roomMemberRepository.save(roomMember);
        roomRepository.save(room);
        return ResponseDto.setSuccess("스터디 룸 생성 성공", new RoomResponseDto(room));
    }

    /* 스터디 룸 수정 */
    @Transactional
    public ResponseDto<RoomResponseDto> updateRoom(Long roomId, RoomRequestDto requestDto, MultipartFile image, Member member) throws IOException  {
        roomRepository.findByRoomId(roomId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );

        roomMemberRepository.findByMemberIdAndRoomMaster(member, true).orElseThrow(
                () -> new CustomException(INVALID_USER)
        );

        String imageUrl = image != null ? uploadImage(image) : "대표 이미지 URL";

        Room room = Room.builder()
                .roomName(requestDto.getRoomName())
                .roomContent(requestDto.getRoomContent())
                .imageUrl(imageUrl)
                .build();

        room.updateRoom(requestDto);

        return ResponseDto.setSuccess("스터디 룸 수정 성공", new RoomResponseDto(room));
    }

    /* 스터디 룸 삭제 */
    @Transactional
    public ResponseDto<RoomResponseDto> deleteRoom(Long roomId, Member member) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );

        roomMemberRepository.findByMemberIdAndRoomMaster(member, true).orElseThrow(
                () -> new CustomException(INVALID_USER)
        );

        roomRepository.delete(room);
        return ResponseDto.setSuccess("스터디 룸 삭제 성공");
    }

    /* 스터디 룸 목록 조회 */
    @Transactional(readOnly = true)
    public ResponseDto<List<RoomResponseDto>> allRooms() {
        List<Room> rooms = roomRepository.findAllByOrderByCreatedAtDesc();
        List<RoomResponseDto> roomResponseDtos = rooms.stream()
                .map(RoomResponseDto::new)
                .collect(Collectors.toList());
        return ResponseDto.setSuccess("스터디 룸 목록 조회 성공", roomResponseDtos);
    }

    /* 스터디 룸 정보 조회 */
    @Transactional(readOnly = true)
    public ResponseDto<RoomResponseDto> infoRoom(Long roomId) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );
        return ResponseDto.setSuccess("스터디 룸 정보 조회 성공", new RoomResponseDto(room));
    }

    /* 이미지 업로드 */
    private String uploadImage(MultipartFile image) throws IOException {
        // 파일명 부여
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        int millis = now.get(ChronoField.MILLI_OF_SECOND);

        String imageName = "image" + hour + minute + second + millis;
        String fileExtension = '.' + image.getOriginalFilename().replaceAll("^.*\\.(.*)$", "$1");
        String fullImageName = "S3" + imageName + fileExtension;

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(image.getContentType());
        objectMetadata.setContentLength(image.getSize());

        InputStream inputStream = image.getInputStream();

        amazonS3.putObject(new PutObjectRequest(bucketName, fullImageName, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3.getUrl(bucketName, fullImageName).toString();
    }
}