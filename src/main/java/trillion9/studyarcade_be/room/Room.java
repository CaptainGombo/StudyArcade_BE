package trillion9.studyarcade_be.room;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import trillion9.studyarcade_be.global.AuditingEntity;
import trillion9.studyarcade_be.room.dto.RoomCreateRequestDto;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@AttributeOverride(name = "memberId", column = @Column(name = "room_creator_id"))
public class Room extends AuditingEntity {
    // OpenVidu 세션 ID
    @Id
    private String sessionId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String roomContent;

    private String category;
    private String imageUrl;
    private String roomPassword;
    private LocalDate expirationDate;

    @ColumnDefault("0")
    private int userCount;

    @ColumnDefault("false")
    private boolean secret;

    @Builder
    private Room(String sessionId, String roomName, String roomContent, String category, String imageUrl, int userCount, boolean secret, String roomPassword, LocalDate expirationDate) {
        this.sessionId = sessionId;
        this.roomName = roomName;
        this.roomContent = roomContent;
        this.category = category;
        this.imageUrl = imageUrl;
        this.userCount = userCount;
        this.secret = secret;
        this.roomPassword = roomPassword;
        this.expirationDate = expirationDate;
    }

    public void updateRoom(RoomCreateRequestDto requestDto, String imageUrl) {
        this.roomName = requestDto.getRoomName();
        this.roomContent = requestDto.getRoomContent();
        this.imageUrl = imageUrl;
    }

    public void updateUserCount(int userCount) {
        this.userCount = userCount;
    }
}