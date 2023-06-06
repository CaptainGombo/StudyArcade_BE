package trillion9.studyarcade_be.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Controller
public class ChatController {

	private final ChatService ChatService;
	@MessageMapping("/chat/message")
	public void message(ChatRequestDto message) {
		ChatService.message(message);
	}
}
