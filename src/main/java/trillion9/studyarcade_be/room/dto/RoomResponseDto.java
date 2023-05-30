package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.Room;

@Getter
@NoArgsConstructor
public class RoomResponseDto {
	private String sessionId;
	private String roomName;
	private String imageUrl;

	public RoomResponseDto(Room room) {
		this.sessionId = room.getSessionId();
		this.roomName = room.getRoomName();
		this.imageUrl = room.getImageUrl();
	}
}