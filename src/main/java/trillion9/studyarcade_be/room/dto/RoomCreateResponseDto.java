package trillion9.studyarcade_be.room.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RoomCreateResponseDto {
	private String sessionId;
	private String roomName;
	private String roomContent;
	private String imageUrl;
	private boolean secret;
	// private String category;
	// private String roomPassword;

	// 방 생성 시간
	private LocalDateTime createdAt;

	@Builder
	private RoomCreateResponseDto(String sessionId, String roomName, String roomContent, String imageUrl, LocalDateTime createdAt, boolean secret) {
		this.sessionId = sessionId;
		this.roomName = roomName;
		this.roomContent = roomContent;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.secret = secret;
	}
}