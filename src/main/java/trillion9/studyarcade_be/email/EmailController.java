package trillion9.studyarcade_be.email;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import trillion9.studyarcade_be.global.ResponseDto;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/api/members/register/email-confirm")
    public ResponseDto<Object> emailConfirm(@RequestParam String email) throws Exception {

        return emailService.sendSimpleMessage(email);
    }
}