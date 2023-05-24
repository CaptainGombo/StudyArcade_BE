package trillion9.studyarcade_be.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;

import javax.validation.Valid;

@Tag(name = "MemberController", description = "회원가입/로그인 API")
@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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
    public ResponseDto<String> login(@RequestBody MemberRequestDto.login memberRequestDto){
        return  memberService.login(memberRequestDto);
    }

}
