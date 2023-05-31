package trillion9.studyarcade_be.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.jwt.JwtUtil;
import trillion9.studyarcade_be.global.security.UserDetailsImpl;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Tag(name = "MemberController", description = "회원가입/로그인 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final KakaoService kakaoService;

    @Operation(summary = "회원가입 API", description = "회원가입")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "회원 가입 완료")})
    // 회원가입
    @PostMapping("/register")
    public ResponseDto<String> register(@Valid @RequestBody MemberRequestDto memberRequestDto) {
        return memberService.register(memberRequestDto);
    }

    @Operation(summary = "로그인 API", description = "로그인")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "로그인 완료")})
    // 로그인
    @PostMapping("/login")
    public ResponseDto<String> login(@RequestBody MemberRequestDto.login memberRequestDto, HttpServletResponse response){
        return  memberService.login(memberRequestDto, response);
    }

    @Operation(summary = "로그아웃 API", description = "로그아웃")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "로그아웃 완료")})
    // 로그아웃
    @PostMapping("/logout")
    public ResponseDto<String> logout(HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return  memberService.logout(request, userDetails.getMember());
    }

    @Operation(summary = "카카오 로그인 API", description = "카카오 로그인")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "카카오 로그인 완료")})
    // 소셜 로그인 - 카카오
    @GetMapping("/kakao/callback")
    public ResponseDto<String> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        // code: 카카오 서버로부터 받은 인가 코드
        String createToken = kakaoService.kakaoLogin(code, response);

        // Cookie 생성 및 직접 브라우저에 Set
        Cookie cookie = new Cookie(JwtUtil.ACCESS_TOKEN, createToken.substring(7));
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseDto.setSuccess("redirect:/api/main");
    }

    @Operation(summary = "닉네임 중복 확인 API", description = "닉네임 중복 확인")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",description = "닉네임 중복 확인 완료")})
    // 닉네임 중복 확인
    @GetMapping("/checkNickname/{nickname}")
    public ResponseDto<Boolean> checkNickname(@PathVariable("nickname") String nickname) {
        return memberService.checkNickname(nickname);
    }
}
