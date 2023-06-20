package trillion9.studyarcade_be.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.jwt.JwtUtil;
import trillion9.studyarcade_be.member.dto.KakaoUserInfoDto;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public ResponseDto<KakaoUserInfoDto> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 3. 필요시에 회원가입
        Member kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);
        kakaoUserInfo.setNickname(kakaoUser.getNickname());

        // 4. JWT 토큰 반환
        String createToken =  jwtUtil.createToken(kakaoUser.getEmail(), "Access_Token");

        // RefreshToken 생성
        String createRefreshToken = jwtUtil.createToken(kakaoUserInfo.getEmail(), "Refresh_Token");

        // TTL 세팅과 함께 새 토큰으로 업데이트 및 저장
        redisTemplate.opsForValue().set("RT:" + kakaoUser.getEmail(), createRefreshToken, JwtUtil.REFRESH_TOKEN_TIME, TimeUnit.MILLISECONDS);

        response.addHeader(JwtUtil.ACCESS_TOKEN, createToken);
        response.addHeader(JwtUtil.REFRESH_TOKEN, createRefreshToken);

        return ResponseDto.setSuccess("카카오 로그인 성공", kakaoUserInfo);
    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "6e564224f0572ff66cb98eee4f48945c");
        body.add("client_secret", "qsMuKI6UCJ4Ng65rQKJJk02ZZFQjJR5m");
        body.add("redirect_uri", "https://www.study-hub.shop/api/members/kakao/callback");
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new KakaoUserInfoDto(id, nickname, email);
    }

    // 3. 필요시에 회원가입
    @Transactional
    public Member registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        Member kakaoUser = memberRepository.findByKakaoId(kakaoId)
                .orElse(null);
        if (kakaoUser == null) {
            // 카카오 사용자 email 동일한 email 가진 회원이 있는지 확인
            String kakaoEmail = kakaoUserInfo.getEmail();
            Member sameEmailUser = memberRepository.findByEmail(kakaoEmail).orElse(null);
            if (sameEmailUser != null) {
                kakaoUser = sameEmailUser;
                // 기존 회원정보에 카카오 Id 추가
                kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
            } else {
                // 신규 회원가입
                // password: random UUID
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);

                // email: kakao email
                String email = kakaoUserInfo.getEmail();
                String nickname;

                Optional<Member> member = memberRepository.findByNickname(kakaoUserInfo.getNickname());

                if (member.isPresent()) {
                    Random random = new Random();
                    int randomNumber = random.nextInt(100);
                    nickname = kakaoUserInfo.getNickname() + randomNumber;
                } else {
                    nickname = kakaoUserInfo.getNickname();
                }
                kakaoUser = Member.builder()
                        .dailyStudyTime(0L)
                        .totalStudyTime(0L)
                        .nickname(nickname)
                        .kakaoId(kakaoId)
                        .password(encodedPassword)
                        .email(email)
                        .build();
            }
            memberRepository.save(kakaoUser);
        }
        return kakaoUser;
    }
}

