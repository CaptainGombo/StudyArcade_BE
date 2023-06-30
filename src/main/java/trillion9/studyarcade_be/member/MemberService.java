package trillion9.studyarcade_be.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.S3Util;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.global.exception.ErrorCode;
import trillion9.studyarcade_be.global.jwt.JwtAuthFilter;
import trillion9.studyarcade_be.global.jwt.JwtUtil;
import trillion9.studyarcade_be.global.jwt.TokenDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;
import trillion9.studyarcade_be.member.dto.MemberResponseDto;
import trillion9.studyarcade_be.member.dto.MyPageResponseDto;
import trillion9.studyarcade_be.member.dto.TopRankedResponseDto;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;
import trillion9.studyarcade_be.room.repository.RoomRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.LinkedHashMap;

import static trillion9.studyarcade_be.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtAuthFilter jwtAuthFilter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final S3Util s3Util;


    @Transactional
    public ResponseDto<String> register(MemberRequestDto memberRequestDto) {
        if (!memberRequestDto.getPassword().equals(memberRequestDto.getCheckPassword())) {
            throw new CustomException(INVALID_USER_PASSWORD);
        }
        String encodedPassword = passwordEncoder.encode(memberRequestDto.getPassword());

        memberRepository.findByEmail(memberRequestDto.getEmail()).ifPresent(member -> {
            throw new CustomException(INVALID_USER_EXISTENCE);
        });

        Member member = Member.builder()
                .email(memberRequestDto.getEmail())
                .password(encodedPassword)
                .nickname(memberRequestDto.getNickname())
                .dailyStudyTime(0L)
                .totalStudyTime(0L)
                .build();

        memberRepository.save(member);

        return ResponseDto.setSuccess("회원가입 성공");
    }

    public ResponseDto<String> login(final MemberRequestDto.login loginRequestDto, final HttpServletResponse response) {

        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        // 멤버 조회
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (member.getKakaoId() != null) {
            throw new CustomException(KAKAO_ID_EXIST);
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(INVALID_USER_PASSWORD);
        }

        // Token 생성
        TokenDto tokenDto = jwtUtil.createAllToken(member.getEmail());

        // TTL 세팅과 함께 새 토큰으로 업데이트 및 저장
        redisTemplate.opsForValue().set("RT:" + member.getEmail(), tokenDto.getRefreshToken(), JwtUtil.REFRESH_TOKEN_TIME, TimeUnit.MILLISECONDS);

        response.addHeader(JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());

        return ResponseDto.setSuccess("로그인 성공", member.getNickname());
    }

    public ResponseDto<String> logout(HttpServletRequest request, Member member) {
        String accessToken = jwtUtil.resolveToken(request, JwtUtil.ACCESS_TOKEN);
        // 로그아웃 하고 싶은 토큰이 유효한 지 먼저 검증하기
        if (!jwtUtil.validateToken(accessToken)) {
            throw new CustomException(INVALID_TOKEN);
        }

        // Redis에서 해당 User email로 저장된 Refresh Token 이 있는지 여부를 확인 후에 있을 경우 삭제를 한다.
        if (redisTemplate.opsForValue().get("RT:" + member.getEmail()) != null) {
            // Refresh Token을 삭제
            redisTemplate.delete("RT:" + member.getEmail());
        }

        // 해당 Access Token 유효시간을 가지고 와서 BlackList에 저장하기
        redisTemplate.opsForValue().set("BL:" + accessToken, "", jwtUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);

        return ResponseDto.setSuccess("로그아웃 성공");
    }

    public ResponseDto<Boolean> checkNickname(String nickname) {
        Optional<Member> member = memberRepository.findByNickname(nickname);
        return ResponseDto.setSuccess("닉네임 중복 확인 완료", member.isEmpty());
    }

    public ResponseDto<String> newAccessToken(HttpServletRequest request) {
        String refreshToken = jwtUtil.resolveToken(request, JwtUtil.REFRESH_TOKEN);
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.createToken(jwtUtil.getUserInfoFromToken(refreshToken), JwtUtil.ACCESS_TOKEN);
        jwtAuthFilter.setAuthentication(jwtUtil.getUserInfoFromToken(newAccessToken.substring(7)));
        return ResponseDto.setSuccess("New Access Token", newAccessToken);
    }

    public ResponseDto<MyPageResponseDto> myPage(Member member) {
        HashOperations<String, String, Long> hash = redisTemplate.opsForHash();
        LocalDate now = LocalDate.now();

        // 마지막 7일 통계
        Map<String, Long> dailyStudyChart = hash.entries(member.getId() + "D");
        dailyStudyChart = dailyStudyChart.entrySet().stream()
                .filter(entry -> {
                    LocalDate entryDate = LocalDate.parse(entry.getKey());
                    return !entryDate.isBefore(now.minusDays(6)); // 오늘을 포함하여 최근 7일치 데이터 가져오기
                })
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        // 마지막 7주 통계
        Map<String, Long> weeklyStudyChart = hash.entries(member.getId() + "W");
        // 마지막 7달 통계
        Map<String, Long> monthlyStudyChart = hash.entries(member.getId() + "M");

        // 다음 등급까지 남은 시간 조회
        Long nextGradeRemainingTime = getNextGradeRemainingTime(member);

        // 총 공부시간 랭킹 1~3위 정보 조회
        List<Object[]> topRankedList = memberRepository.findTopRanked();

        List<TopRankedResponseDto> topRankedDtoList = IntStream.range(0, topRankedList.size())
                .mapToObj(index -> {
                    Object[] topRanked = topRankedList.get(index);
                    String nickname = String.valueOf(topRanked[0]);
                    String title = String.valueOf(topRanked[1]);
                    Long totalStudyTime = Long.parseLong(topRanked[2].toString());
                    int rank = index + 1; // 순서 할당
                    return new TopRankedResponseDto(rank, nickname, title, totalStudyTime);
                })
                .toList();

        // 내가 만든 방 리스트 조회
        List<RoomResponseDto> myRooms = roomRepository.findAllByMemberId(member.getId());

        // 회원 정보 설정
        MyPageResponseDto responseDto = MyPageResponseDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .imageUrl(member.getImageUrl())
                .dailyStudyTime(member.getDailyStudyTime())
                .totalStudyTime(member.getTotalStudyTime())
                .title(member.getTitle())
                .topRankedList(topRankedDtoList)
                .nextGradeRemainingTime(nextGradeRemainingTime)
                .dailyStudyChart(dailyStudyChart)
                .weeklyStudyChart(weeklyStudyChart)
                .monthlyStudyChart(monthlyStudyChart)
                .myRooms(myRooms)
                .build();

        return ResponseDto.setSuccess("마이페이지 조회 성공", responseDto);
    }

    public ResponseDto<MemberResponseDto> getProfile(Member member) {

        //다음 등급까지 남은 시간 조회
        Long nextGradeRemainingTime = getNextGradeRemainingTime(member);

        MemberResponseDto responseDto = MemberResponseDto.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .imageUrl(member.getImageUrl())
                .title(member.getTitle())
                .nextGradeRemainingTime(nextGradeRemainingTime)
                .totalStudyTime(member.getTotalStudyTime())
                .build();

        return ResponseDto.setSuccess("프로필 조회 성공", responseDto);
    }

    @Transactional
    public ResponseDto<MemberResponseDto> updateProfile(MemberRequestDto memberRequestDto, MultipartFile image, Member member) throws IOException {

        String imageUrl = (image == null || image.isEmpty()) ? member.getImageUrl() : s3Util.uploadImage(image);

        if (memberRequestDto == null || memberRequestDto.getNickname() == null) {
            memberRequestDto.setNickname(member.getNickname());
        }

        String encodedPassword = member.getPassword();

        if (memberRequestDto.getPassword() != null || memberRequestDto.getCheckPassword() != null) {
            if (!memberRequestDto.getPassword().equals(memberRequestDto.getCheckPassword())) {
                throw new CustomException(ErrorCode.INVALID_PASSWORD_MATCH);
            }
            encodedPassword = passwordEncoder.encode(memberRequestDto.getPassword());
        }
        member.updateMember(memberRequestDto, imageUrl, encodedPassword);
        memberRepository.save(member);

        return ResponseDto.setSuccess("프로필 변경 성공");
    }

    // 다음 등급까지 남은 시간 조회
    public Long getNextGradeRemainingTime(Member member) {
        Long totalStudyTime = member.getTotalStudyTime();

        if (totalStudyTime >= 1501 * 60) {
            return 0L; // 이미 최고 등급인 경우 남은 시간은 0으로 처리
        }

        long nextGradeRemainingTime;

        if (totalStudyTime >= 1001 * 60) {
            nextGradeRemainingTime = (1501 * 60) - totalStudyTime;
        } else if (totalStudyTime >= 651 * 60) {
            nextGradeRemainingTime = (1001 * 60) - totalStudyTime;
        } else if (totalStudyTime >= 401 * 60) {
            nextGradeRemainingTime = (651 * 60) - totalStudyTime;
        } else if (totalStudyTime >= 201 * 60) {
            nextGradeRemainingTime = (401 * 60) - totalStudyTime;
        } else if (totalStudyTime >= 51 * 60) {
            nextGradeRemainingTime = (201 * 60) - totalStudyTime;
        } else {
            nextGradeRemainingTime = (51 * 60) - totalStudyTime;
        }
        return nextGradeRemainingTime;
    }

}