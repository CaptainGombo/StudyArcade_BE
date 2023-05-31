package trillion9.studyarcade_be.room;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.ConnectionType;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.room.dto.RoomCreateRequestDto;
import trillion9.studyarcade_be.room.dto.RoomCreateResponseDto;
import trillion9.studyarcade_be.room.dto.RoomDetailResponseDto;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;
import trillion9.studyarcade_be.roommember.RoomMember;
import trillion9.studyarcade_be.roommember.RoomMemberRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static trillion9.studyarcade_be.global.exception.ErrorCode.INVALID_USER;
import static trillion9.studyarcade_be.global.exception.ErrorCode.ROOM_NOT_FOUND;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final AmazonS3 amazonS3;

    //openvidu 서버 키 값 주입
    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openvidu;

    private Long roomMaxUser = 9L;

    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    /* 스터디 룸 목록 조회 */
    @Transactional(readOnly = true)
    public ResponseDto<List<RoomResponseDto>> allRooms(int page) {
        // 페이징 처리
        Pageable pageable = PageRequest.of(page , 6);

        // pageable을 메소드에 전달
        Page<Room> rooms = roomRepository.findAll(pageable);

        List<RoomResponseDto> roomResponseDtos = rooms.getContent().stream()
            .map(RoomResponseDto::new)
            .collect(Collectors.toList());

        return ResponseDto.setSuccess("스터디 룸 목록 조회 성공", roomResponseDtos);
    }

    /* 스터디 룸 정보 조회 */
    @Transactional(readOnly = true)
    public ResponseDto<RoomDetailResponseDto> infoRoom(String sessionId) {
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
            () -> new CustomException(ROOM_NOT_FOUND)
        );
        return ResponseDto.setSuccess("스터디 룸 정보 조회 성공", new RoomDetailResponseDto(room));
    }

    /* 스터디 룸 생성 */
    @Transactional
    public ResponseDto<RoomCreateResponseDto> createRoom(RoomCreateRequestDto requestDto, MultipartFile image, Member member)
        throws Exception {

        /* Session Id 셋팅 */
        RoomCreateResponseDto newToken = createSession(member);

        log.info("user 정보 : " + member.getEmail());
        log.info("user 정보 : " + member.getNickname());

        String imageUrl = (image != null || !image.isEmpty()) ? uploadImage(image) : "대표 이미지 URL";

        Room room = Room.builder()
                        .sessionId(newToken.getSessionId())
                        .roomName(requestDto.getRoomName())
                        .roomContent(requestDto.getRoomContent())
                        .imageUrl(imageUrl)
                        .build();

        RoomMember roomMember = new RoomMember(member);
        roomMember.setRoomMaster(true);

        roomMemberRepository.save(roomMember);
        roomRepository.save(room);

        RoomCreateResponseDto responseDto = RoomCreateResponseDto.builder()
                                                .sessionId(room.getSessionId())
                                                .roomName(room.getRoomName())
                                                .roomContent(room.getRoomContent())
                                                .imageUrl(room.getImageUrl())
                                                .build();

        return ResponseDto.setSuccess("스터디 룸 생성 성공", responseDto);
    }

    /* 스터디 룸 수정 */
    @Transactional
    public ResponseDto<RoomDetailResponseDto> updateRoom(String sessionId, RoomCreateRequestDto requestDto, MultipartFile image, Member member)
        throws IOException  {
        roomRepository.findBySessionId(sessionId).orElseThrow(
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

        return ResponseDto.setSuccess("스터디 룸 수정 성공", new RoomDetailResponseDto(room));
    }

    /* 스터디 룸 삭제 */
    @Transactional
    public ResponseDto<RoomDetailResponseDto> deleteRoom(String sessionId, Member member) {
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );

        roomMemberRepository.findByMemberIdAndRoomMaster(member, true).orElseThrow(
                () -> new CustomException(INVALID_USER)
        );

        roomRepository.delete(room);
        return ResponseDto.setSuccess("스터디 룸 삭제 성공");
    }
    /* 스터디 룸 입장 */
    @Transactional
    public String enterRoom(String sessionId, Member member)
        throws OpenViduJavaClientException, OpenViduHttpException {

        /* 해당 sessionId를 가진 스터디룸이 존재하는지 확인한다. */
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
            () -> new EntityNotFoundException("해당 방이 없습니다."));


        /* 스터디 룸의 최대 인원은 9명으로 제한하고, 초과 시 예외를 발생시킨다. */
        synchronized (room) {
            room.updateUserCount(room.getUserCount() + 1);

            if (room.getUserCount() > roomMaxUser) {
                /* 트랜잭션에 의해 위의 updateCntUser 메서드의 user수 +1 자동으로 롤백(-1)되어서 9에 맞추어짐. */
                throw new IllegalArgumentException("방이 가득찼습니다.");
            }
        }

        // /* 비공개 방일 경우, 비밀번호 체크를 수행한다. */
        // if (!room.isPrivat()) {
        //     if (requestData == null || requestData.getPassword() == null) {    // 패스워드를 입력 안했을 때 에러 발생
        //         throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        //     }
        //     if (!room.getPassword().equals(requestData.getPassword())) {  // 비밀번호가 틀리면 에러 발생
        //         throw new IllegalArgumentException("비밀번호가 틀립니다.");
        //     }
        // }

        /* 이미 입장한 유저일 경우 예외를 발생시킨다. */
        Optional<RoomMember> alreadyEnterChatRoomUser
            = roomMemberRepository.findByMemberIdAndSessionIdAndIsOut(member.getId(), sessionId, false);

        if (alreadyEnterChatRoomUser.isPresent()) throw new IllegalArgumentException("이미 입장한 멤버입니다.");

        /* 방 입장 토큰 생성 */
        String roomToken = createToken(member, room.getSessionId());

        RoomMember roomMember = RoomMember.builder()
            .member(member)
            .sessionId(sessionId)
            .roomMaster(false)
            .roomToken(roomToken)
            .build();

        /* 현재 방에 접속한 사용자 저장 */
        roomMemberRepository.save(roomMember);

        /* 채팅방 정보를 저장한다. */
        roomRepository.save(room);

        return "Success";
    }

    /* 스터디 룸 퇴장 */
    @Transactional
    public String outRoom(String sessionId, Member member, Duration roomStudyTime) {

        /* 방이 있는 지 확인 */
        Room room = roomRepository.findBySessionIdAndIsDelete(sessionId, false).orElseThrow(
            () -> new EntityNotFoundException("채팅방이 존재하지않습니다.")
        );

        /* 방에 멤버가 존재하는지 확인. */
        RoomMember roomMember = roomMemberRepository.findByMemberIdAndSessionIdAndIsOut(member.getId(), sessionId, false).orElseThrow(
            () -> new EntityNotFoundException("방에 있는 멤버가 아닙니다.")
        );

        /* 이미 해당 방에서 나간 유저 표시. */
        if (roomMember.isOut()) {
            throw new IllegalArgumentException("이미 방에서 나간 유저 입니다.");
        }

        /* 총 누적 시간 업데이트 */
        roomMember.getMember().updateStudyTime(roomStudyTime);

        /* 스터디 룸 유저 삭제 */
        roomMember.deleteRoomMember();

        // /* 스터디 룸 유저 수 확인
        //  * 스터디 룸 유저가 0명이라면 방 논리삭제. */
        // synchronized (chatRoom) {
        //     /* 방 인원 카운트 - 1 */
        //     chatRoom.updateCntUser(chatRoom.getCntUser() - 1);
        //
        //     if (chatRoom.getCntUser() <= 0) {
        //         /*방 논리 삭제 + 방 삭제된 시간 기록*/
        //         LocalDateTime roomDeleteTime = Timestamp.valueOf(LocalDateTime.now()).toLocalDateTime();
        //         chatRoom.deleteRoom(roomDeleteTime);
        //         return "Success";
        //     }
        //
        //     /* 스터디룸의 유저 수가 1명 이상있다면 유저 수만 변경 */
        //     return "Success";
        //
        // }
        return "Success";
    }

    /* 스터디 룸 생성 시 세션 발급 */
    private RoomCreateResponseDto createSession(Member member) throws
        OpenViduJavaClientException, OpenViduHttpException {

        /* 사용자 연결 시 닉네임 전달 */
        String serverData = member.getNickname();

        /* serverData을 사용하여 connectionProperties 객체를 빌드 */
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder()
            .type(ConnectionType.WEBRTC)
            .data(serverData)
            .build();

        /* 새로운 OpenVidu 세션(스터디 룸) 생성 */
        Session session = openvidu.createSession();


        /* 스터디룸 생성 시 방을 만들며, 방장이 들어가지게 구현하려면 아래의 코드로 토큰 바로 발급
            방 생성, 방 입장(방장 입장) 로직이 나누어져 있다면 토큰 발급 필요 없음.
        String token = session.createConnection(connectionProperties).getToken();
        */

        return RoomCreateResponseDto.builder()
            .sessionId(session.getSessionId()) //리턴해주는 해당 세션아이디로 다른 유저 채팅방 입장시 요청해주시면 됩니다.
            .build();
    }

    /* 스터디룸 입장 시 토큰 발급 */
    private String createToken(Member member, String sessionId) throws
        OpenViduJavaClientException, OpenViduHttpException {

        /* 입장하는 유저의 이름을 server data에 저장 */
        String serverData = member.getNickname();

        /* serverData을 사용하여 connectionProperties 객체를 빌드 */
        ConnectionProperties connectionProperties
            = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).data(serverData).build();


        openvidu.fetch();


        /*Openvidu Server에 활성화되어 있는 세션(채팅방) 목록을 가지고 온다.*/
        List<Session> activeSessionList = openvidu.getActiveSessions();


        /* 세션 리스트에서 요청자가 입력한 세션 ID가 일치하는 세션을 찾아서 새로운 토큰을 생성
         * 없다면, Openvidu Server에 해당 방이 존재하지 않는 것이므로, 익셉션 발생 */
        Session session = activeSessionList.stream()
            .filter(s -> s.getSessionId().equals(sessionId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("채팅세션이 존재하지 않습니다."));

        /*해당 채팅방에 프로퍼티스를 설정하면서 커넥션을 만들고, 방에 접속할 수 있는 토큰을 발급한다.*/
        return session.createConnection(connectionProperties).getToken();
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