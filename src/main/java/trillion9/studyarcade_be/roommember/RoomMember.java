package trillion9.studyarcade_be.roommember;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import trillion9.studyarcade_be.global.AuditingEntity;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@AttributeOverride(name = "createdAt", column = @Column(name = "room_enter_time"))
public class RoomMember extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_member_id")
    private Long id;
    private String sessionId;

    @ColumnDefault("false")
    private boolean roomMaster;

    @Builder
    private RoomMember(String sessionId, boolean roomMaster) {
        this.sessionId = sessionId;
        this.roomMaster = roomMaster;
    }

    public void setRoomMaster(boolean roomMaster) {
        this.roomMaster = roomMaster;
    }
}
