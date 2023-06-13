package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.Room;

@Getter
@NoArgsConstructor
public class RoomResponseDto {
	private String sessionId;
	private String roomName;
	private String roomContent;
	private String imageUrl;
	private int userCount;
	private String category;
	private String createdAt;

	public RoomResponseDto(Room room) {
		this.sessionId = room.getSessionId();
		this.roomName = room.getRoomName();
		this.roomContent = room.getRoomContent();
		this.imageUrl = room.getImageUrl();
		this.userCount = room.getUserCount();
		this.category = room.getCategory();
		this.createdAt = room.getCreatedAt();
	}
}