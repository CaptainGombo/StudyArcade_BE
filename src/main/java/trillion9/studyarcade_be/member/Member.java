package trillion9.studyarcade_be.member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Long dailyStudyTime;
    @Column
    private Long totalStudyTime;

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

    public void updateStudyTime(Long roomStudyTime) {
        this.dailyStudyTime += roomStudyTime;
    }

    public void updateTotalStudyTime() {
        this.totalStudyTime += this.dailyStudyTime;
        this.dailyStudyTime = 0L;
    }
}