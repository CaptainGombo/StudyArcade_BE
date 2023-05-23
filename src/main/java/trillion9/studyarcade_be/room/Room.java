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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String roomContent;

    @ColumnDefault("false")
    private boolean roomMaster;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Room(RoomRequestDto requestDto, Member member) {
        this.roomName = requestDto.getRoomName();
        this.roomContent = requestDto.getRoomContent();
        this.member = member;
    }

    public void updateRoom(RoomRequestDto requestDto) {
        this.roomName = requestDto.getRoomName();
        this.roomContent = requestDto.getRoomContent();
    }
}
