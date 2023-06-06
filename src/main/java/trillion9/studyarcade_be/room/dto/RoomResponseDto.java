package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RoomResponseDto {
	private String sessionId;
	private String roomName;
	private String roomContent;
	private String imageUrl;
	private Long userCount;
	private LocalDateTime createdAt;
}