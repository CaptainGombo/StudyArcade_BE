package trillion9.studyarcade_be.room;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.global.Timestamp;
import trillion9.studyarcade_be.room.dto.RoomCreateRequestDto;

import javax.persistence.*;

import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor
public class Room extends Timestamp {

    // 세션 ID
    @Id
    private String sessionId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String roomContent;

    @Column
    private String imageUrl;

    @Column
    private Long userCount;

    // @ColumnDefault("false")
    // private boolean isPrivate;

    // @Column
    // private String roomPassword;

    @ColumnDefault("false")
    private boolean isDelete;

    @Column
    private LocalDateTime roomDeleteTime;

    @Builder
    private Room(String sessionId, String roomName, String roomContent, String imageUrl, Long userCount) {
        this.sessionId = sessionId;
        this.roomName = roomName;
        this.roomContent = roomContent;
        this.imageUrl = imageUrl;
        this.userCount = userCount;
    }

    public void deleteRoom(LocalDateTime roomDeleteTime) {
        this.isDelete = true;
        this.roomDeleteTime = roomDeleteTime;
    }

    public void updateRoom(RoomCreateRequestDto requestDto) {
        this.roomName = requestDto.getRoomName();
        this.roomContent = requestDto.getRoomContent();
    }

    public void updateUserCount(Long userCount) {
        this.userCount = userCount;
    }
}
