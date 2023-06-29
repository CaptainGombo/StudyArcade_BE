package trillion9.studyarcade_be.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import trillion9.studyarcade_be.global.ResponseDto;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.member.MemberRepository;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Random;

import static trillion9.studyarcade_be.global.exception.ErrorCode.INVALID_USER_EXISTENCE;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final MemberRepository memberRepository;
    private final JavaMailSender emailSender;

    public String ePw;

    private MimeMessage createMessage(String to)throws Exception {
        String ePw = createKey();
        System.out.println("보내는 대상 : "+ to);
        System.out.println("인증 번호 : "+ ePw);
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, to);
        message.setSubject("스터브 회원가입 인증 코드입니다.");

        String msg = "";
        msg += "<head><title>스터브 회원가입 인증</title></head>"
            + "<div style='max-width: 500px; margin: 50px auto; background-color: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);'>"
            + "<h1 style='color: #1E3C72; font-size: 32px; margin-top: 0; text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);'>안녕하세요, 스터브입니다.</h1>"
            + "<p style='color: #333; font-size: 18px; margin-bottom: 10px; line-height: 1.4;'>아래 코드를 복사하여 입력해주세요.</p>"
            + "<p style='color: #333; font-size: 18px; margin-bottom: 10px; line-height: 1.4;'>감사합니다.</p>"
            + "<h3 style='color: #1E3C72; font-size: 18px; margin-bottom: 15px;'>회원가입 인증 코드</h3>"
            + "<div><strong style='font-size: 22px; color: #0D47A1; background-color: #E1F5FE; padding: 12px 20px; border-radius: 5px; display: inline-block; text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);'>";
        msg += ePw + "</strong></div></div>";
        message.setText(msg, "utf-8", "html"); // 내용
        message.setFrom(new InternetAddress("studyhu6@gmail.com","StudyHub")); // 보내는 사람

        this.ePw = ePw;
        return message;
    }

    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return key.toString();
    }

    @Override
    public ResponseDto<Object> sendSimpleMessage(String to) throws Exception {
        // TODO Auto-generated method stub

        Optional<Member> memberOptional = memberRepository.findByEmail(to);
        if (memberOptional.isPresent()) {
            throw new CustomException(INVALID_USER_EXISTENCE);
        }

        MimeMessage message = createMessage(to);
        try { // 예외처리
            emailSender.send(message);
        } catch (MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException(es.getMessage());
        }
        return ResponseDto.setSuccess("이메일 발송 성공", ePw);
    }
}