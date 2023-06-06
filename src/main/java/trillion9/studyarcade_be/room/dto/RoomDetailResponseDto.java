package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RoomDetailResponseDto {
    private String sessionId;
    private String roomName;
    private String roomContent;
    private String imageUrl;
    private LocalDateTime createdAt;
}