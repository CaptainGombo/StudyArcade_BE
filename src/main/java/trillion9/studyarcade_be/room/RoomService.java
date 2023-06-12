package trillion9.studyarcade_be.room;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.openvidu.java.client.*;
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
import trillion9.studyarcade_be.global.exception.ErrorCode;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.room.dto.*;
import trillion9.studyarcade_be.room.repository.RoomFilterImpl;
import trillion9.studyarcade_be.room.repository.RoomRepository;
import trillion9.studyarcade_be.roommember.RoomMember;
import trillion9.studyarcade_be.roommember.RoomMemberRepository;
import trillion9.studyarcade_be.roommember.RoomMemberResponseDto;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

import static trillion9.studyarcade_be.global.exception.ErrorCode.INVALID_USER;
import static trillion9.studyarcade_be.global.exception.ErrorCode.ROOM_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomFilterImpl roomFilter;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final AmazonS3 amazonS3;

    //openvidu 서버 키 값
    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;
    private OpenVidu openvidu;

    private int roomMaxUser = 9;

    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    /* 스터디 룸 목록 조회 */
    @Transactional(readOnly = true)
    public ResponseDto<Page<RoomResponseDto>> allRooms(int page, String category, String keyword) {
        // 페이징 처리
        Pageable pageable = PageRequest.of(page , 6);
        Page<RoomResponseDto> roomResponseDtos = roomFilter.findRooms(pageable, category, keyword);

        if(roomResponseDtos.isEmpty() && page != 0){
            throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        }

        return ResponseDto.setSuccess("스터디 룸 목록 조회 성공", roomResponseDtos);
    }


    /* 스터디 룸 생성 */
    @Transactional
    public ResponseDto<RoomCreateResponseDto> createRoom(RoomCreateRequestDto requestDto, MultipartFile image, Member member)
        throws Exception {

         /* SessionId 셋팅 */
        RoomCreateResponseDto newToken = createSession(member);

        log.info("user 정보 : " + member.getEmail());
        log.info("user 정보 : " + member.getNickname());

        String imageUrl = (image == null || image.isEmpty()) ? "대표 이미지 URL" : uploadImage(image);

        Room room = Room.builder()
                        .sessionId(newToken.getSessionId())
                        .roomName(requestDto.getRoomName())
                        .roomContent(requestDto.getRoomContent())
                        .userCount(1)
                        .imageUrl(imageUrl)
                        .secret(requestDto.isSecret())
                        .category(requestDto.getCategory())
                        .roomPassword(requestDto.getRoomPassword())
                        .expirationDate(requestDto.getExpirationDate())
                        .build();

        RoomMember roomMember = RoomMember.builder()
                                .sessionId(newToken.getSessionId())
                                .roomMaster(true)
                                .build();

        roomMemberRepository.save(roomMember);
        roomRepository.save(room);

        RoomCreateResponseDto responseDto = RoomCreateResponseDto.builder()
                                                .sessionId(room.getSessionId())
                                                .roomName(room.getRoomName())
                                                .roomContent(room.getRoomContent())
                                                .imageUrl(room.getImageUrl())
                                                .secret(room.isSecret())
                                                .category(room.getCategory())
                                                .build();

        return ResponseDto.setSuccess("스터디 룸 생성 성공", responseDto);
    }

    /*채팅방 + 채팅방에 속한 유저 정보 불러오기*/
    @Transactional
    public ResponseDto<RoomDetailResponseDto> getRoomData(String sessionId, Member member) {

        /*방이 있는 지 확인*/
        Room room = roomRepository.findBySessionIdAndIsDelete(sessionId, false).orElseThrow(
                () -> new EntityNotFoundException("해당 방이 없습니다."));

        /*해당 방에 해당 유저가 접속해 있는 상태여아
         * 방유저 정보 불러오기 API를 사용할 수 있다.*/
        roomMemberRepository.findByMemberIdAndSessionId(member.getId(), sessionId).orElseThrow(
                () -> new IllegalArgumentException("방에 유저가 존재하지 않습니다.")
        );

        /*채팅방 유저들 Entity*/
        List<RoomMember> roomMemberList =
                roomMemberRepository.findAllBySessionId(room.getSessionId());

        /*채팅방 유저들 Dto*/
        List<RoomMemberResponseDto> roomMemberResponseDtos = roomMemberList.stream()
                                                                .map(RoomMemberResponseDto::new)
                                                                .toList();

        RoomDetailResponseDto roomDetailResponseDto = new RoomDetailResponseDto(room, roomMemberResponseDtos);

        return ResponseDto.setSuccess("스터디룸 정보 조회 성공", roomDetailResponseDto);
    }

    /* 스터디 룸 수정 */
    @Transactional
    public ResponseDto<String> updateRoom(String sessionId, RoomCreateRequestDto requestDto, MultipartFile image, Member member)
        throws IOException  {
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );

        roomMemberRepository.findByMemberIdAndSessionIdAndRoomMaster(member.getId(), sessionId, true).orElseThrow(
                () -> new CustomException(INVALID_USER)
        );

        String imageUrl = (image == null || image.isEmpty()) ? "대표 이미지 URL" : uploadImage(image);

        room.updateRoom(requestDto, imageUrl);

        return ResponseDto.setSuccess("스터디 룸 수정 성공");
    }

    /* 스터디 룸 삭제 */
    @Transactional
    public ResponseDto<String> deleteRoom(String sessionId, Member member) {
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );

        roomMemberRepository.findByMemberIdAndSessionIdAndRoomMaster(member.getId(), sessionId, true).orElseThrow(
                () -> new CustomException(INVALID_USER)
        );

        roomRepository.delete(room);
        return ResponseDto.setSuccess("스터디 룸 삭제 성공");
    }

    /* 스터디 룸 입장 */
    @Transactional
    public ResponseDto<String> enterRoom(RoomEnterRequestDto requestDto, String sessionId, Member member)
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

         /* 비공개 방일 경우, 비밀번호 체크를 수행한다. */
         if (room.isSecret()) {
             if (requestDto == null || requestDto.getRoomPassword() == null) {    // 패스워드를 입력 안했을 때 에러 발생
                 throw new IllegalArgumentException("비밀번호를 입력해주세요.");
             }
             if (!room.getRoomPassword().equals(requestDto.getRoomPassword())) {  // 비밀번호가 틀리면 에러 발생
                 throw new IllegalArgumentException("비밀번호가 틀립니다.");
             }
         }

        /* 이미 입장한 유저일 경우 예외를 발생시킨다. */
        Optional<RoomMember> alreadyEnterChatRoomUser
            = roomMemberRepository.findByMemberIdAndSessionId(member.getId(), sessionId);

        if (alreadyEnterChatRoomUser.isPresent()) throw new IllegalArgumentException("이미 입장한 멤버입니다.");

        /* 방 입장 토큰 생성 */
        String roomToken = createToken(member, room.getSessionId());

        RoomMember roomMember = RoomMember.builder()
//                .member(member)
                .sessionId(sessionId)
                .roomMaster(false)
                .roomToken(roomToken)
                .build();

        /* 현재 방에 접속한 사용자 저장 */
        roomMemberRepository.save(roomMember);

        /* 채팅방 정보를 저장한다. */
        roomRepository.save(room);

        return ResponseDto.setSuccess("스터디 룸 입장 성공");
    }

    /* 스터디 룸 퇴장 */
    @Transactional
    public ResponseDto<String> outRoom(String sessionId, Long studyTime, Member member) {

        /* 방이 있는 지 확인 */
        Room room = roomRepository.findBySessionIdAndIsDelete(sessionId, false).orElseThrow(
            () -> new EntityNotFoundException("채팅방이 존재하지않습니다.")
        );

        /* 방에 멤버가 존재하는지 확인. */
        RoomMember roomMember = roomMemberRepository.findByMemberIdAndSessionId(member.getId(), sessionId).orElseThrow(
            () -> new EntityNotFoundException("방에 있는 멤버가 아닙니다.")
        );

         /* 하루 누적 시간 업데이트 */
        member.updateStudyTime(studyTime);

        /* 스터디 룸 유저 삭제 */
        roomMemberRepository.delete(roomMember);

        /* 방 인원 카운트 - 1 */
        room.updateUserCount(room.getUserCount() - 1);

        return ResponseDto.setSuccess("스터디 룸 퇴장 성공");
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
          */
         String token = session.createConnection(connectionProperties).getToken();

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