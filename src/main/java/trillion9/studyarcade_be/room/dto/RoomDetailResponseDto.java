package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.Room;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RoomDetailResponseDto {
    private String sessionId;
    private String roomName;
    private String roomContent;
    private String imageUrl;
    private LocalDateTime createdAt;

    public RoomDetailResponseDto(Room room) {
        this.sessionId = room.getSessionId();
        this.roomName = room.getRoomName();
        this.roomContent = room.getRoomContent();
        this.imageUrl = room.getImageUrl();
        this.createdAt = room.getCreatedAt();
    }
}