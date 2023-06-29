package trillion9.studyarcade_be.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import trillion9.studyarcade_be.room.Room;

@Getter
@AllArgsConstructor
public class RoomResponseDto {
	private String sessionId;
	private String roomName;
	private String roomContent;
	private String imageUrl;
	private int userCount;
	private boolean secret;
	private String category;

	public RoomResponseDto(Room room) {
		this.sessionId = room.getSessionId();
		this.roomName = room.getRoomName();
		this.roomContent = room.getRoomContent();
		this.imageUrl = room.getImageUrl();
		this.userCount = room.getUserCount();
		this.category = room.getCategory();
		this.secret = room.isSecret();
	}

}