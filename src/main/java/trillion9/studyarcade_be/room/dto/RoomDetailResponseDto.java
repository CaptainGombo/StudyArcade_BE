package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.room.Room;
import trillion9.studyarcade_be.roommember.RoomMemberResponseDto;

import java.util.List;

@Getter
@NoArgsConstructor
public class RoomDetailResponseDto {
    private String sessionId;
    private String roomName;
    private String roomContent;
    private String category;
    private int userCount;

    private List<RoomMemberResponseDto> members;

    public RoomDetailResponseDto(Room room, List<RoomMemberResponseDto> roomMemberResponseDtos) {
        this.sessionId = room.getSessionId();
        this.roomName = room.getRoomName();
        this.roomContent = room.getRoomContent();
        this.category = room.getCategory();
        this.userCount = room.getUserCount();

        this.members = roomMemberResponseDtos;
    }
}