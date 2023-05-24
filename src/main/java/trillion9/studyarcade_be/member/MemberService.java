package trillion9.studyarcade_be.member;

import static trillion9.studyarcade_be.global.exception.ErrorCode.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public ResponseDto<String> login(MemberRequestDto.login memberRequestDto) {
        return ResponseDto.setSuccess("임시");
    }
}
