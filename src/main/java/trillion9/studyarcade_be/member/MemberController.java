package trillion9.studyarcade_be.member;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;

import javax.validation.Valid;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseDto<String> register(@Valid @RequestBody MemberRequestDto memberRequestDto) {
        return memberService.register(memberRequestDto);
    }

}
