package trillion9.studyarcade_be.room;

import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.S3Util;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.member.MemberRepository;
import trillion9.studyarcade_be.room.dto.*;
import trillion9.studyarcade_be.room.repository.RoomFilterImpl;
import trillion9.studyarcade_be.room.repository.RoomRepository;
import trillion9.studyarcade_be.roommember.RoomMember;
import trillion9.studyarcade_be.roommember.RoomMemberRepository;
import trillion9.studyarcade_be.roommember.RoomMemberResponseDto;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static trillion9.studyarcade_be.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomFilterImpl roomFilter;
    private final S3Util s3Util;
    private final EntityManager entityManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;
    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openvidu;

    private final int roomMaxUser = 9;

    @PostConstruct
    public void init() {
        openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    Calendar calendar = Calendar.getInstance();

    /* 스터디 룸 목록 조회 */
    public ResponseDto<Page<RoomResponseDto>> allRooms(int page, String category, String keyword) {
        // 페이징 처리
        Pageable pageable = PageRequest.of(page , 6);
        Page<RoomResponseDto> roomResponseDtos = roomFilter.findRooms(pageable, category, keyword);

        return ResponseDto.setSuccess("스터디 룸 목록 조회 성공", roomResponseDtos);
    }

    /* 스터디 룸 생성 */
    @Transactional
    public ResponseDto<RoomCreateResponseDto> createRoom(RoomCreateRequestDto requestDto, MultipartFile image, Member member)
        throws Exception {

        Long myRoomCount = roomRepository.countAllByMemberId(member.getId());

        if (myRoomCount > 2) {
            throw new CustomException(INVALID_ROOM_COUNT);
        }

         /* SessionId 셋팅 */
        RoomCreateResponseDto newToken = createSession(member);

        String imageUrl = (image == null || image.isEmpty()) ? "대표 이미지 URL" : s3Util.uploadImage(image);

        RoomMember roomMember = RoomMember.builder()
                                .sessionId(newToken.getSessionId())
                                .roomMaster(true)
                                .build();

        roomMemberRepository.save(roomMember);

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

        entityManager.persist(room);

        RoomCreateResponseDto responseDto = RoomCreateResponseDto.builder()
                                                .sessionId(room.getSessionId())
                                                .roomName(room.getRoomName())
                                                .roomContent(room.getRoomContent())
                                                .imageUrl(room.getImageUrl())
                                                .secret(room.isSecret())
                                                .userCount(1)
                                                .category(room.getCategory())
                                                .build();

        return ResponseDto.setSuccess("스터디 룸 생성 성공", responseDto);
    }

    /* 채팅방 + 채팅방에 속한 유저 정보 불러오기 */
    public ResponseDto<RoomDetailResponseDto> getRoomData(String sessionId, Member member) {

        /*방이 있는 지 확인*/
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND));

        /*해당 방에 해당 유저가 접속해 있는 상태여아
         * 방유저 정보 불러오기 API를 사용할 수 있다.*/
        roomMemberRepository.findByMemberIdAndSessionId(member.getId(), sessionId).orElseThrow(
                () -> new CustomException(ROOM_MEMBER_NOT_FOUND)
        );

        /* 채팅방 유저들 Entity */
        List<RoomMember> roomMemberList = roomMemberRepository.findAllBySessionId(room.getSessionId());

        /* 채팅방 유저들 Dto */
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

        roomRepository.findBySessionIdAndMemberId(sessionId, member.getId())
                .orElseThrow(() -> new CustomException(INVALID_USER));

        String imageUrl = (image == null || image.isEmpty()) ? "대표 이미지 URL" : s3Util.uploadImage(image);

        room.updateRoom(requestDto, imageUrl);

        return ResponseDto.setSuccess("스터디 룸 수정 성공");
    }

    /* 스터디 룸 삭제 */
    @Transactional
    public ResponseDto<String> deleteRoom(String sessionId, Member member) throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND)
        );

        roomRepository.findBySessionIdAndMemberId(sessionId, member.getId())
                .orElseThrow(() -> new CustomException(INVALID_USER));

        openvidu.fetch();
        List<Session> activeSessionList = openvidu.getActiveSessions();
        Optional<Session> sessionOptional = activeSessionList.stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .findFirst();
        if (sessionOptional.isPresent()) {
            Session session = sessionOptional.get();
            session.close();
        }

        List<RoomMember> roomMembers = roomMemberRepository.findAllBySessionId(sessionId);
        roomMemberRepository.deleteAll(roomMembers);

        roomRepository.delete(room);
        return ResponseDto.setSuccess("스터디 룸 삭제 성공");
    }

    /* 스터디 룸 입장 */
    @Transactional
    public ResponseDto<String> enterRoom(RoomEnterRequestDto requestDto, String sessionId, Member member)
        throws OpenViduJavaClientException, OpenViduHttpException {

        openvidu.fetch();
        /*Openvidu Server에 활성화되어 있는 세션(채팅방) 목록을 가지고 온다.*/
        List<Session> activeSessionList = openvidu.getActiveSessions();

        Optional<Session> sessionOptional = activeSessionList.stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .findFirst();

        Session session;
        if (sessionOptional.isPresent()) {
            session = sessionOptional.get();
        } else {
            // 세션을 새로 생성
            session = openvidu.createSession(new SessionProperties.Builder().customSessionId(sessionId).build());
        }

        /* 해당 sessionId를 가진 스터디룸이 존재하는지 확인 */
        Room room = roomRepository.findBySessionId(session.getSessionId()).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND));

         /* 비공개 방일 경우, 비밀번호 체크를 수행 */
         if (room.isSecret()) {
             if (requestDto == null || requestDto.getRoomPassword() == null) {    // 패스워드를 입력 안했을 때 에러 발생
                 throw new CustomException(INVALID_PASSWORD_INPUT);
             }
             if (!room.getRoomPassword().equals(requestDto.getRoomPassword())) {  // 비밀번호가 틀리면 에러 발생
                 throw new CustomException(INVALID_PASSWORD_MATCH);
             }
         }

        /* 이미 입장한 유저일 경우 예외 발생 */
        Optional<RoomMember> alreadyEnterChatRoomUser
            = roomMemberRepository.findByMemberIdAndSessionId(member.getId(), session.getSessionId());

        if (alreadyEnterChatRoomUser.isPresent()) throw new CustomException(MEMBER_ALREADY_ENTERED);

        // 방 입장 하나로 제한
//        Optional<RoomMember> roomMemberCheck = roomMemberRepository.findByMemberId(member.getId());
//
//        if (roomMemberCheck.isPresent()) {
//            throw new CustomException(ROOM_MEMBER_LIMIT_EXCEEDED);
//        }

        /* 스터디 룸의 최대 인원은 9명으로 제한하고, 초과 시 예외 발생 */
        synchronized (room) {
            room.updateUserCount(room.getUserCount() + 1);

            if (room.getUserCount() > roomMaxUser) {
                /* 트랜잭션에 의해 위의 updateCntUser 메서드의 user수 +1 자동으로 롤백(-1)되어서 9에 맞추어짐. */
                throw new CustomException(ROOM_FULL);
            }
        }
//
//        room.updateUserCount(room.getUserCount() + 1);
//
//        if (room.getUserCount() > roomMaxUser) {
//            throw new CustomException(ROOM_FULL);
//        }

        /* 방 입장 토큰 생성 */
        String roomToken = createToken(member, room.getSessionId());

        RoomMember roomMember = RoomMember.builder()
                .sessionId(session.getSessionId())
                .roomMaster(false)
                .roomToken(roomToken)
                .build();

        Optional<Room> roomMasterCheck = roomRepository.findBySessionIdAndMemberId(sessionId, member.getId());

        if (roomMasterCheck.isPresent()) {
            roomMember.setRoomMaster(true);
        }

        /* 현재 방에 접속한 사용자 저장 */
        roomMemberRepository.save(roomMember);

        /* 채팅방 정보 저장 */
        roomRepository.save(room);

        return ResponseDto.setSuccess("스터디 룸 입장 성공");
    }

    /* 스터디 룸 퇴장 */
    @Transactional
    public ResponseDto<String> outRoom(String sessionId, Long studyTime, Member member) {

        /* 방이 있는 지 확인 */
        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
            () -> new CustomException(ROOM_NOT_FOUND)
        );

        /* 방에 멤버가 존재하는지 확인 */
        RoomMember roomMember = roomMemberRepository.findByMemberIdAndSessionId(member.getId(), sessionId).orElseThrow(
            () -> new CustomException(ROOM_MEMBER_NOT_FOUND)
        );

        /* 하루 누적 시간 업데이트 */
        member.updateStudyTime(studyTime);
        memberRepository.save(member);

        /* 통계 공부 시간 업데이트 */
        String currentDate = LocalDate.now().toString();
        String currentWeek = String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
        String currentMonth = calendar.get(Calendar.YEAR) + "." + (calendar.get(Calendar.MONTH) + 1);

        HashOperations<String, String, Long> hash = redisTemplate.opsForHash();
        hash.increment(member.getId() + "D", currentDate, studyTime);
        hash.increment(member.getId() + "W", currentWeek, studyTime);
        hash.increment(member.getId() + "M", currentMonth, studyTime);

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
//         String serverData = member.getNickname();

         /* serverData을 사용하여 connectionProperties 객체를 빌드 */
//         ConnectionProperties connectionProperties = new ConnectionProperties.Builder()
//             .type(ConnectionType.WEBRTC)
//             .data(serverData)
//             .build();

         /* 새로운 OpenVidu 세션(스터디 룸) 생성 */
         Session session = openvidu.createSession();

         /* 스터디룸 생성 시 방을 만들며, 방장이 들어가지게 구현하려면 아래의 코드로 토큰 바로 발급
             방 생성, 방 입장(방장 입장) 로직이 나누어져 있다면 토큰 발급 필요 없음.
          */
         // String token = session.createConnection(connectionProperties).getToken();

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
            .orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));

        /*해당 채팅방에 프로퍼티스를 설정하면서 커넥션을 만들고, 방에 접속할 수 있는 토큰을 발급한다.*/
        return session.createConnection(connectionProperties).getToken();
    }
}