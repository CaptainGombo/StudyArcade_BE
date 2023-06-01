package trillion9.studyarcade_be.roommember;

import java.sql.Time;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import trillion9.studyarcade_be.member.Member;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column
    private Long sessionId;

    @Column
    private String roomToken;

    @ColumnDefault("false")
    private boolean roomMaster;

    @Column
    private boolean isOut;

    public RoomMember(Member member) {
        this.member = member;
    }

    public void setRoomMaster(boolean roomMaster) {
        this.roomMaster = roomMaster;
    }

    @Builder
    private RoomMember(Member member, Long sessionId, String roomToken, boolean roomMaster) {
        this.member = member;
        this.sessionId = sessionId;
        this.roomToken = roomToken;
        this.roomMaster = roomMaster;
    }

    public void deleteRoomMember() {
        this.isOut = true;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
