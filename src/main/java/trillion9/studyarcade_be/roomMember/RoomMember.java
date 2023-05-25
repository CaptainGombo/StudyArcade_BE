package trillion9.studyarcade_be.roomMember;

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
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @ColumnDefault("false")
    private boolean roomMaster;

    public void setRoomMaster(boolean roomMaster, Member member) {
        this.roomMaster = roomMaster;
        this.member = member;
    }
}
