package trillion9.studyarcade_be.roommember;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RoomMemberResponseDto {
    private Long roomMemberId;
    private String nickname;
    private String sessionId;
    private String roomToken;
    private boolean roomMaster;

    public RoomMemberResponseDto(RoomMember roomMember) {
        this.roomMemberId = roomMember.getId();
        this.nickname = roomMember.getMember().getNickname();
        this.sessionId = roomMember.getSessionId();
        this.roomToken = roomMember.getRoomToken();
        this.roomMaster = roomMember.isRoomMaster();
    }
}
