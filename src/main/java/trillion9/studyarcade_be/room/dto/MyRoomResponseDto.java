package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyRoomResponseDto {
    private String roomName;
    private String category;

    public MyRoomResponseDto(String roomName, String category) {
        this.roomName = roomName;
        this.category = category;
    }
}
