package trillion9.studyarcade_be.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.global.exception.ErrorCode;
import trillion9.studyarcade_be.global.jwt.JwtUtil;
import trillion9.studyarcade_be.global.jwt.TokenDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletResponse response;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private MemberService memberService;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @DisplayName("신규 회원가입 성공")
    @Test
    void register_ValidMemberRequestDto_Success() {
        // Given
        MemberRequestDto memberRequestDto = MemberRequestDto.builder()
                .email("test@example.com")
                .password("password")
                .checkPassword("password")
                .nickname("test")
                .build();

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember("test@example.com", "encodedPassword", "test"));

        // When
        ResponseDto<String> response = memberService.register(memberRequestDto);

        // Then
        assertNull(response.getData());
        assertEquals("회원가입 성공", response.getMessage());
    }

    @DisplayName("회원가입 시 패스워드, 체크 패스워드 불일치 시 예외 반환")
    @Test
    void register_PasswordMismatch_ThrowsException() {
        // Given
        MemberRequestDto memberRequestDto = MemberRequestDto.builder()
                .email("test@example.com")
                .password("password1")
                .checkPassword("password2")
                .nickname("test")
                .build();

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.register(memberRequestDto));

        // Then
        assertEquals(ErrorCode.INVALID_USER_PASSWORD, exception.getErrorCode());
    }

    @DisplayName("이미 가입된 이메일로 회원가입 시 예외 반환")
    @Test
    void register_DuplicateEmail_ThrowsException() {
        // Given
        MemberRequestDto memberRequestDto = MemberRequestDto.builder()
                .email("test@example.com")
                .password("password")
                .checkPassword("password")
                .nickname("test")
                .build();

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember("test@example.com", "encodedPassword", "test")));

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.register(memberRequestDto));

        // Then
        assertEquals(ErrorCode.INVALID_USER_EXISTENCE, exception.getErrorCode());
    }

    @DisplayName("로그인 성공")
    @Test
    void login_ValidCredentials_Success() {
        // Given
        MemberRequestDto.login loginRequestDto = MemberRequestDto.login.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Member member = Member.builder()
                .email("test@example.com")
                .password("$2a$10$uW84yBCuYZOmNL4Rq1g5Su3qYSoW7z5lcZwWlNzCsb5.Khq2VnEWa")
                .nickname("test")
                .build();

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("your_access_token_value_here")
                .refreshToken("your_refresh_token_value_here")
                .build();

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.createAllToken(anyString())).thenReturn(tokenDto);

        // When
        ResponseDto<String> responseDto = memberService.login(loginRequestDto, response);

        // Then
        assertNotNull(responseDto);
        assertEquals("로그인 성공", responseDto.getMessage());
        assertEquals("test", responseDto.getData());

        // Verify response headers
        verify(response).addHeader(JwtUtil.ACCESS_TOKEN, "your_access_token_value_here");
        verify(response).addHeader(JwtUtil.REFRESH_TOKEN, "your_refresh_token_value_here");

        // Verify Redis operations
        verify(redisTemplate).opsForValue();
        verify(redisTemplate.opsForValue()).set(eq("RT:" + member.getEmail()), eq("your_refresh_token_value_here"), eq(JwtUtil.REFRESH_TOKEN_TIME), eq(TimeUnit.MILLISECONDS));
    }

    @DisplayName("가입되지 않은 이메일로 로그인 시 에러 반환")
    @Test
    void login_InvalidEmail_ThrowsException() {
        // Given
        MemberRequestDto.login loginRequestDto = MemberRequestDto.login.builder()
                .email("test@example.com")
                .password("password")
                .build();

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.login(loginRequestDto, response));

        // Then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("로그인 중 패스워드 불일치 시 에러 반환")
    @Test
    void login_IncorrectPassword_ThrowsException() {
        // Given
        MemberRequestDto.login loginRequestDto = MemberRequestDto.login.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Member member = Member.builder()
                .email("test@example.com")
                .password("$2a$10$uW84yBCuYZOmNL4Rq1g5Su3qYSoW7z5lcZwWlNzCsb5.Khq2VnEWa")
                .nickname("test")
                .build();

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.login(loginRequestDto, response));

        // Then
        assertEquals(ErrorCode.INVALID_USER_PASSWORD, exception.getErrorCode());
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout_ValidToken_Success() {
        // Given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Member member = Member.builder()
                .email("test@example.com")
                .build();

        String accessToken = "valid_access_token";
        when(jwtUtil.resolveToken(request, JwtUtil.ACCESS_TOKEN)).thenReturn(accessToken);
        when(jwtUtil.validateToken(accessToken)).thenReturn(true);
        when(redisTemplate.opsForValue().get("RT:" + member.getEmail())).thenReturn("valid_refresh_token");

        // When
        ResponseDto<String> response = memberService.logout(request, member);

        // Then
        assertNotNull(response);
        assertEquals("로그아웃 성공", response.getMessage());

        // Verify Redis operations
        verify(redisTemplate).delete("RT:" + member.getEmail());
        verify(redisTemplate.opsForValue(), times(1)).set(eq("BL:" + accessToken), eq(""), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @DisplayName("닉네임 중복 체크 - 사용 가능한 닉네임일 시 반환")
    @Test
    void checkNickname_NonexistentNickname_ReturnsTrue() {
        // Given
        String nickname = "nonexistent_nickname";
        when(memberRepository.findByNickname(nickname)).thenReturn(Optional.empty());

        // When
        ResponseDto<Boolean> response = memberService.checkNickname(nickname);

        // Then
        assertNotNull(response);
        assertEquals("닉네임 중복 확인 완료", response.getMessage());
        assertTrue(response.getData() != null);
    }

    @DisplayName("닉네임 중복 체크 - 이미 사용중인 닉네임일 시")
    @Test
    void checkNickname_ExistentNickname_ReturnsFalse() {
        // Given
        String nickname = "existent_nickname";
        Member member = Member.builder()
                .nickname(nickname)
                .build();
        when(memberRepository.findByNickname(nickname)).thenReturn(Optional.of(member));

        // When
        ResponseDto<Boolean> response = memberService.checkNickname(nickname);

        // Then
        assertNotNull(response);
        assertEquals("닉네임 중복 확인 완료", response.getMessage());
        assertNotNull(response.getData());
        assertFalse(response.getData());
    }
    private Member testMember(String email, String password, String nickname) {
        return Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
    }

}