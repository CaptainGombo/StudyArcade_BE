package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.Room;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RoomResponseDto {
	private String sessionId;
	private String roomName;
	private String roomContent;
	private String roomPassword;
	private String imageUrl;
	private int userCount;
	private boolean secret;
	private String category;
	private LocalDateTime createdAt;

	public RoomResponseDto(Room room) {
		this.sessionId = room.getSessionId();
		this.roomName = room.getRoomName();
		this.roomContent = room.getRoomContent();
		this.roomPassword = room.getRoomPassword();
		this.imageUrl = room.getImageUrl();
		this.userCount = room.getUserCount();
		this.category = room.getCategory();
		this.secret = room.isSecret();
		this.createdAt = room.getCreatedAt();
	}
}