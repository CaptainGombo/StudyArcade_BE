package trillion9.studyarcade_be.room;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import trillion9.studyarcade_be.global.Timestamp;
import trillion9.studyarcade_be.room.dto.RoomCreateRequestDto;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Room extends Timestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 세션 ID

    private String sessionId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String roomContent;

    @Column
    private String imageUrl;

    @ColumnDefault("0")
    private Long userCount;

    @ColumnDefault("false")
    private boolean secret;

    @Column
    private String roomPassword;

    @ColumnDefault("false")
    private boolean isDelete;

    @Column
    private LocalDateTime roomDeleteTime;

    @Builder
    private Room(String sessionId, String roomName, String roomContent, String imageUrl, Long userCount, boolean secret, String roomPassword) {
        this.sessionId = sessionId;
        this.roomName = roomName;
        this.roomContent = roomContent;
        this.imageUrl = imageUrl;
        this.userCount = userCount;
        this.secret = secret;
        this.roomPassword = roomPassword;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}