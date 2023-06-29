package trillion9.studyarcade_be.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.member.MemberRepository;

import javax.mail.internet.MimeMessage;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmailServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("이메일 인증코드 메일 발송 성공")
    @Test
    void sendSimpleMessage_ValidEmail_Success() throws Exception {
        // Arrange
        String to = "nonexisting@example.com";

        doReturn(Optional.empty()).when(memberRepository).findByEmail(to);
        doReturn(mock(MimeMessage.class)).when(emailSender).createMimeMessage();
        doNothing().when(emailSender).send(any(MimeMessage.class));

        // Act
        emailService.sendSimpleMessage(to);

        // Assert
        verify(memberRepository, times(1)).findByEmail(to);
        verify(emailSender, times(1)).createMimeMessage();
        verify(emailSender, times(1)).send(any(MimeMessage.class));
    }

    @DisplayName("이미 사용중인 이메일에 인증코드 발송 시도 시 예외 반환")
    @Test
    void sendSimpleMessage_ExistingEmail_ThrowsException() throws Exception {
        // Arrange
        String to = "existing@example.com";

        doReturn(Optional.of(mock(Member.class))).when(memberRepository).findByEmail(to);

        // Act & Assert
        assertThrows(CustomException.class, () -> emailService.sendSimpleMessage(to));

        verify(memberRepository, times(1)).findByEmail(to);
        verify(emailSender, never()).createMimeMessage();
        verify(emailSender, never()).send(any(MimeMessage.class));
    }

}