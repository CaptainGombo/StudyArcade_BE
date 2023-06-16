package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyRoomResponseDto {
    private String roomName;
    private String roomContent;

    public MyRoomResponseDto(String roomName, String roomContent) {
        this.roomName = roomName;
        this.roomContent = roomContent;
    }
}
