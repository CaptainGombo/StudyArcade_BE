package trillion9.studyarcade_be.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
	private String sessionId; // 방 세션 id
	private String nickname; // 채팅을 보낸 사람 닉네임
	private String message; // 메시지
	private String createdAt; // 채팅 발송 시간
	private String profileImage; //유저 프로필 이미지

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}




