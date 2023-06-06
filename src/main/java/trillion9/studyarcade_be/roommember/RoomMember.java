package trillion9.studyarcade_be.roommember;

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
    @Column(name = "room_member_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column
    private String sessionId;

    @Column
    private String roomToken;

    @ColumnDefault("false")
    private boolean roomMaster;

    @Builder
    private RoomMember(Member member, String sessionId, String roomToken, boolean roomMaster) {
        this.member = member;
        this.sessionId = sessionId;
        this.roomToken = roomToken;
        this.roomMaster = roomMaster;
    }
}
