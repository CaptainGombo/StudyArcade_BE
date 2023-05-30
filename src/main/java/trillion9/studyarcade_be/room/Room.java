package trillion9.studyarcade_be.room;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import trillion9.studyarcade_be.global.Timestamp;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.room.dto.RoomRequestDto;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class Room extends Timestamp {

    @Id
    private String roomId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String roomContent;

    @Column
    private String imageUrl;

    @Column
    private Long userCount;



    @ColumnDefault("false")
    private boolean isPrivate;

    // @Column
    // private String roomPassword;

    @Builder
    private Room(String roomId, String roomName, String roomContent, String imageUrl) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomContent = roomContent;
        this.imageUrl = imageUrl;
    }

    public void updateRoom(RoomRequestDto requestDto) {
        this.roomName = requestDto.getRoomName();
        this.roomContent = requestDto.getRoomContent();
    }

    public void updateUserCount(Long userCount) {
        this.userCount = userCount;
    }
}
