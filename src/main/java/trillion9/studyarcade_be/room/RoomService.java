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

    public ResponseDto<Page<RoomResponseDto>> allRooms(int page, String category, String keyword) {
        // 페이징 처리
        Pageable pageable = PageRequest.of(page , 6);
        Page<RoomResponseDto> roomResponseDtos = roomFilter.findRooms(pageable, category, keyword);

        return ResponseDto.setSuccess("스터디 룸 목록 조회 성공", roomResponseDtos);
    }

    @Transactional
    public ResponseDto<RoomCreateResponseDto> createRoom(RoomCreateRequestDto requestDto, MultipartFile image, Member member)
        throws Exception {

        Long myRoomCount = roomRepository.countAllByMemberId(member.getId());

        if (myRoomCount > 2) {
            throw new CustomException(INVALID_ROOM_COUNT);
        }

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

    // 채팅방 + 채팅방에 속한 유저 정보 불러오기
    public ResponseDto<RoomDetailResponseDto> getRoomData(String sessionId, Member member) {

        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND));

        // 해당 방에 해당 유저가 접속해 있는 상태인지 확인
        roomMemberRepository.findByMemberIdAndSessionId(member.getId(), sessionId).orElseThrow(
                () -> new CustomException(ROOM_MEMBER_NOT_FOUND)
        );

        List<RoomMember> roomMemberList = roomMemberRepository.findAllBySessionId(room.getSessionId());

        List<RoomMemberResponseDto> roomMemberResponseDtos = roomMemberList.stream()
                                                                .map(RoomMemberResponseDto::new)
                                                                .toList();

        RoomDetailResponseDto roomDetailResponseDto = new RoomDetailResponseDto(room, roomMemberResponseDtos);

        return ResponseDto.setSuccess("스터디룸 정보 조회 성공", roomDetailResponseDto);
    }

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

    @Transactional
    public ResponseDto<String> enterRoom(RoomEnterRequestDto requestDto, String sessionId, Member member)
        throws OpenViduJavaClientException, OpenViduHttpException {

        openvidu.fetch();

        List<Session> activeSessionList = openvidu.getActiveSessions();

        Optional<Session> sessionOptional = activeSessionList.stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .findFirst();

        Session session;
        if (sessionOptional.isPresent()) {
            session = sessionOptional.get();
        } else {
            // 같은 sessionId로 Custom 세션을 새로 생성
            session = openvidu.createSession(new SessionProperties.Builder().customSessionId(sessionId).build());
        }

        // 해당 sessionId를 가진 스터디룸이 존재하는지 확인
        Room room = roomRepository.findBySessionId(session.getSessionId()).orElseThrow(
                () -> new CustomException(ROOM_NOT_FOUND));

         // 비공개 방일 경우, 비밀번호 체크를 수행
         if (room.isSecret()) {
             if (requestDto == null || requestDto.getRoomPassword() == null) {    // 패스워드를 입력 안했을 때 에러 발생
                 throw new CustomException(INVALID_PASSWORD_INPUT);
             }
             if (!room.getRoomPassword().equals(requestDto.getRoomPassword())) {  // 비밀번호가 틀리면 에러 발생
                 throw new CustomException(INVALID_PASSWORD_MATCH);
             }
         }

        // 이미 입장한 유저일 경우 예외 발생 & 방 입장 하나로 제한
        Optional<RoomMember> roomMemberCheck = roomMemberRepository.findByMemberId(member.getId());

        if (roomMemberCheck.isPresent()) throw new CustomException(ROOM_MEMBER_LIMIT_EXCEEDED);

        // 스터디 룸의 최대 인원은 9명으로 제한하고, 초과 시 예외 발생
        synchronized (room) {
            room.updateUserCount(room.getUserCount() + 1);
            if (room.getUserCount() > roomMaxUser) {
                throw new CustomException(ROOM_FULL);
            }
        }

        // 방 입장 토큰 생성
//        String roomToken = createToken(member, room.getSessionId());

        RoomMember roomMember = RoomMember.builder()
                .sessionId(session.getSessionId())
                .roomMaster(false)
                .build();

        Optional<Room> roomMasterCheck = roomRepository.findBySessionIdAndMemberId(sessionId, member.getId());

        if (roomMasterCheck.isPresent()) {
            roomMember.setRoomMaster(true);
        }

        // 현재 방 및 방에 접속한 사용자 저장
        roomMemberRepository.save(roomMember);
        roomRepository.save(room);

        return ResponseDto.setSuccess("스터디 룸 입장 성공");
    }

    @Transactional
    public ResponseDto<String> outRoom(String sessionId, Long studyTime, Member member) {

        Room room = roomRepository.findBySessionId(sessionId).orElseThrow(
            () -> new CustomException(ROOM_NOT_FOUND)
        );

        RoomMember roomMember = roomMemberRepository.findByMemberIdAndSessionId(member.getId(), sessionId).orElseThrow(
            () -> new CustomException(ROOM_MEMBER_NOT_FOUND)
        );

        // 하루 누적 시간 업데이트
        member.updateStudyTime(studyTime);
        memberRepository.save(member);

        // 통계 공부 시간 업데이트
        String currentDate = LocalDate.now().toString();
        String currentWeek = String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
        String currentMonth = calendar.get(Calendar.YEAR) + "." + (calendar.get(Calendar.MONTH) + 1);

        HashOperations<String, String, Long> hash = redisTemplate.opsForHash();
        hash.increment(member.getId() + "D", currentDate, studyTime);
        hash.increment(member.getId() + "W", currentWeek, studyTime);
        hash.increment(member.getId() + "M", currentMonth, studyTime);

        roomMemberRepository.delete(roomMember);

        room.updateUserCount(room.getUserCount() - 1);

        return ResponseDto.setSuccess("스터디 룸 퇴장 성공");
    }

     // 스터디 룸 생성 시 세션 생성
     private RoomCreateResponseDto createSession(Member member) throws
         OpenViduJavaClientException, OpenViduHttpException {

         Session session = openvidu.createSession();

         return RoomCreateResponseDto.builder()
             .sessionId(session.getSessionId())
             .build();
     }

    // 스터디룸 입장 시 토큰 발급
    private String createToken(Member member, String sessionId) throws
        OpenViduJavaClientException, OpenViduHttpException {

        String serverData = member.getNickname();

        ConnectionProperties connectionProperties
            = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).data(serverData).build();

        openvidu.fetch();

        List<Session> activeSessionList = openvidu.getActiveSessions();

        Session session = activeSessionList.stream()
            .filter(s -> s.getSessionId().equals(sessionId))
            .findFirst()
            .orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));

        // 해당 채팅방에 프로퍼티스를 설정하면서 커넥션을 만들고, 방에 접속할 수 있는 토큰을 발급
        return session.createConnection(connectionProperties).getToken();
    }
}