package trillion9.studyarcade_be.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.global.security.UserDetailsImpl;
import trillion9.studyarcade_be.member.dto.KakaoUserInfoDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;
import trillion9.studyarcade_be.member.dto.MemberResponseDto;
import trillion9.studyarcade_be.member.dto.MyPageResponseDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static trillion9.studyarcade_be.global.exception.ErrorCode.TOKEN_INEXISTENT;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final KakaoService kakaoService;

    @PostMapping("/register")
    public ResponseDto<String> register(@Valid @RequestBody MemberRequestDto memberRequestDto) {
        return memberService.register(memberRequestDto);
    }

    @PostMapping("/login")
    public ResponseDto<String> login(@RequestBody MemberRequestDto.login memberRequestDto, HttpServletResponse response) {
        return memberService.login(memberRequestDto, response);
    }

    @PostMapping("/logout")
    public ResponseDto<String> logout(HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return  memberService.logout(request, userDetails.getMember());
    }

    @GetMapping("/kakao/callback")
    public ResponseDto<KakaoUserInfoDto> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        return kakaoService.kakaoLogin(code, response);
    }

    @GetMapping("/check-nickname/{nickname}")
    public ResponseDto<Boolean> checkNickname(@PathVariable("nickname") String nickname) {
        return memberService.checkNickname(nickname);
    }

    @GetMapping("/refresh-token")
    public ResponseDto<String> newAccessToken(HttpServletRequest request) {
        return memberService.newAccessToken(request);
    }

    @GetMapping("/mypage")
    public ResponseDto<MyPageResponseDto> myPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return memberService.myPage(userDetails.getMember());
    }

    @GetMapping("/profile")
    public ResponseDto<MemberResponseDto> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return memberService.getProfile(userDetails.getMember());
    }

    @PatchMapping("/profile")
    public ResponseDto<MemberResponseDto> updateProfile(@Valid @RequestPart(value = "content", required = false) MemberRequestDto memberRequestDto,
                                                        @RequestPart(value = "image", required = false) MultipartFile image,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        if (userDetails == null) throw new CustomException(TOKEN_INEXISTENT);
        return memberService.updateProfile(memberRequestDto, image, userDetails.getMember());
    }
}