package trillion9.studyarcade_be.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.global.jwt.JwtAuthFilter;
import trillion9.studyarcade_be.global.jwt.JwtUtil;
import trillion9.studyarcade_be.global.jwt.TokenDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;
import trillion9.studyarcade_be.member.dto.MyPageResponseDto;
import trillion9.studyarcade_be.studytime.StudyTimeRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static trillion9.studyarcade_be.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final StudyTimeRepository studyTimeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtAuthFilter jwtAuthFilter;
    private final RedisTemplate<String, String> redisTemplate;

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
                .build();

        memberRepository.saveAndFlush(member);
        return ResponseDto.setSuccess("회원가입 성공");
    }

    @Transactional
    public ResponseDto<String> login(final MemberRequestDto.login loginRequestDto, final HttpServletResponse response) {

        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        // 멤버 조회
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(INVALID_USER_PASSWORD);
        }

        // Token 생성
        TokenDto tokenDto = jwtUtil.createAllToken(member.getEmail());

        // TTL 세팅과 함께 새 토큰으로 업데이트 및 저장
        redisTemplate.opsForValue().set("RT:" + member.getEmail(), tokenDto.getRefreshToken(), JwtUtil.REFRESH_TOKEN_TIME, TimeUnit.MILLISECONDS);

        // Header에 access token, refresh token 추가
        response.addHeader(JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());

        return ResponseDto.setSuccess("로그인 성공", member.getNickname());
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public ResponseDto<Boolean> checkNickname(String nickname) {
        Optional<Member> member = memberRepository.findByNickname(nickname);
        return ResponseDto.setSuccess("닉네임 중복 확인 완료", member.isEmpty());
    }

    @Transactional(readOnly = true)
    public ResponseDto<String> newAccessToken(HttpServletRequest request) {
        String refreshToken = jwtUtil.resolveToken(request, JwtUtil.REFRESH_TOKEN);
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.createToken(jwtUtil.getUserInfoFromToken(refreshToken), JwtUtil.ACCESS_TOKEN);
        jwtAuthFilter.setAuthentication(jwtUtil.getUserInfoFromToken(newAccessToken.substring(7)));
        return ResponseDto.setSuccess("New Access Token", newAccessToken);
    }


    @Transactional
    public ResponseDto<MyPageResponseDto> myPage(Member member) {

        LocalDate now = LocalDate.now();

        // 마지막 7일 통계
        HashMap<String, Long> dailyStudyChart = new HashMap<>();
        List<Object[]> dailyStudyTime = studyTimeRepository.findStudyTimeByDateRange(member.getId(), now.minusDays(7), now);
        for (Object[] obj : dailyStudyTime) {
            String day = String.valueOf(obj[0]);
            Long studyTime = Long.parseLong(obj[1].toString());
            dailyStudyChart.put(day, studyTime);
        }

        // 마지막 7주 통계
        HashMap<String, Long> weeklyStudyChart = new HashMap<>();
        List<Object[]>  weeklyStudyTime = studyTimeRepository.findStudyTimeByWeekRange(member.getId(), now.minusWeeks(7), now);
        for (Object[] obj : weeklyStudyTime) {
            String week = String.valueOf(obj[0]);
            Long studyTime = Long.parseLong(obj[1].toString());
            weeklyStudyChart.put(week, studyTime);
        }

        // 마지막 7달 통계
        HashMap<String, Long> monthlyStudyChart = new HashMap<>();
        List<Object[]>  monthlyStudyTime = studyTimeRepository.findStudyTimeByMonthRange(member.getId(), now.minusMonths(7), now);
        for (Object[] obj : monthlyStudyTime) {
            String year = String.valueOf(obj[0]);
            String week = String.valueOf(obj[1]);
            Long studyTime = Long.parseLong(obj[2].toString());
            monthlyStudyChart.put(year + "." + week, studyTime);
        }

        // 회원 정보 설정
        MyPageResponseDto responseDto = MyPageResponseDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .dailyStudyTime(member.getDailyStudyTime())
                .totalStudyTime(member.getTotalStudyTime())
                .title(member.getTitle())
                .dailyStudyChart(dailyStudyChart)
                .weeklyStudyChart(weeklyStudyChart)
                .monthlyStudyChart(monthlyStudyChart)
                .build();

        return ResponseDto.setSuccess("마이페이지 조회 성공", responseDto);
    }
}