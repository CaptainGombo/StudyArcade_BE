package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RoomEnterRequestDto {
    private String roomPassword;
}
