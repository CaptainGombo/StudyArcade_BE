package trillion9.studyarcade_be.member;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.roommember.RoomMember;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private Long kakaoId;

    @Column(nullable = false, unique = true)
    private String nickname;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    @Column
    private Duration totalStudyTime;

    @Builder
    private Member(Long kakaoId, String nickname, String email, String password) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public Member kakaoIdUpdate(Long kakaoId) {
        this.kakaoId = kakaoId;
        return this;
    }

public void updateStudyTime(Duration roomStudyTime) {
        this.totalStudyTime = this.totalStudyTime.plus(roomStudyTime);
    }
}
