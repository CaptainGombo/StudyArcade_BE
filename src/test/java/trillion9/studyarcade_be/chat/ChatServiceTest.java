package trillion9.studyarcade_be.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.member.MemberRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChatServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private MemberRepository memberRepository;

    private RedisSubscriber redisSubscriber;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        redisSubscriber = new RedisSubscriber(objectMapper, messagingTemplate, memberRepository);
    }

    @DisplayName("채팅 전송 성공")
    @Test
    public void sendMessage_ValidMessage_Success() throws Exception {
        // Arrange
        String publishMessage = "{\"sessionId\":\"session123\",\"nickname\":\"John\",\"message\":\"Hello\",\"createdAt\":\"2023-06-29 10:00:00\",\"profileImg\":\"profile.jpg\"}";
        ChatMessageDto chatMessage = ChatMessageDto.builder()
                .sessionId("session123")
                .nickname("스터브")
                .message("Hello")
                .createdAt("2023-06-29 10:00:00")
                .profileImg("profile.jpg")
                .build();
        String profileImage = "profile.jpg";
        when(objectMapper.readValue(any(String.class), eq(ChatMessageDto.class))).thenReturn(chatMessage);
        when(memberRepository.findByNickname(eq("John"))).thenReturn(Optional.of(Member.builder().imageUrl(profileImage).build()));

        // Act
        redisSubscriber.sendMessage(publishMessage);

        // Assert
        String expectedTopic = "/sub/chat/room/session123";
        verify(messagingTemplate, times(1)).convertAndSend(eq(expectedTopic), eq(chatMessage));
    }

}