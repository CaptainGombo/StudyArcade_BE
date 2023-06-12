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
    private String roomToken;

    @ColumnDefault("false")
    private boolean roomMaster;

    @Builder
    private RoomMember(String sessionId, String roomToken, boolean roomMaster) {
        this.sessionId = sessionId;
        this.roomToken = roomToken;
        this.roomMaster = roomMaster;
    }
}
