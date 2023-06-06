package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomDetailResponseDto {
    private String roomName;
    private String roomContent;
    private String imageUrl;
    private String category;
}