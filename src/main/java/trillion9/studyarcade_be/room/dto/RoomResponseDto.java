package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.Room;

@Getter
@NoArgsConstructor
public class RoomResponseDto {
	private String roomName;
	private String imageUrl;

	public RoomResponseDto(Room room) {
		this.roomName = room.getRoomName();
		this.imageUrl = room.getImageUrl();
	}
}