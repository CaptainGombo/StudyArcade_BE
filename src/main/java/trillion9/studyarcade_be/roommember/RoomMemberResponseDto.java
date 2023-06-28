package trillion9.studyarcade_be.roommember;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomMemberResponseDto {
    private String nickname;
    private String sessionId;
    private boolean roomMaster;
//    private String roomToken;

    public RoomMemberResponseDto(RoomMember roomMember) {
//        this.nickname = roomMember.getMember().getNickname();
        this.sessionId = roomMember.getSessionId();
        this.roomMaster = roomMember.isRoomMaster();
//        this.roomToken = roomMember.getRoomToken();
    }
}
