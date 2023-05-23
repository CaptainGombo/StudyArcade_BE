package trillion9.studyarcade_be.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;

@Service
public class MemberService {
    @Transactional
    public ResponseDto<String> register(MemberRequestDto memberRequestDto) {
        return ResponseDto.setSuccess("임시");
    }
}
