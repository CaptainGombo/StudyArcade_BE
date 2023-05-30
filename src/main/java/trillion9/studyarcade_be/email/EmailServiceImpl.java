package trillion9.studyarcade_be.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
public class EmailServiceImpl implements EmailService{

    @Autowired
    JavaMailSender emailSender;

    public static final String ePw = createKey();

    private MimeMessage createMessage(String to)throws Exception {
        System.out.println("보내는 대상 : "+ to);
        System.out.println("인증 번호 : "+ ePw);
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, to); // 보내는 대상
        message.setSubject("스터브 회원가입 인증 코드입니다."); // 제목

        String msg = "";
        msg += "<head><title>스터브 회원가입 인증</title></head>"
                + "<div style='max-width: 500px; margin: 50px auto; background-color: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);'>"
                + "<h1 style='color: #1E3C72; font-size: 32px; margin-top: 0; text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);'>안녕하세요, 스터브입니다.</h1>"
                + "<p style='color: #333; font-size: 18px; margin-bottom: 10px; line-height: 1.4;'>아래 코드를 복사하여 입력해주세요.</p>"
                + "<p style='color: #333; font-size: 18px; margin-bottom: 10px; line-height: 1.4;'>감사합니다.</p>"
                + "<h3 style='color: #1E3C72; font-size: 18px; margin-bottom: 15px;'>회원가입 인증 코드</h3>"
                + "<div><strong style='font-size: 22px; color: #0D47A1; background-color: #E1F5FE; padding: 12px 20px; border-radius: 5px; display: inline-block; text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);'>";
        msg += ePw + "</strong></div></div>";
        message.setFrom(new InternetAddress("studyhu6@gmail.com","스터브")); // 보내는 사람

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
    public String sendSimpleMessage(String to)throws Exception {
        // TODO Auto-generated method stub
        MimeMessage message = createMessage(to);
        try{ // 예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return ePw;
    }
}