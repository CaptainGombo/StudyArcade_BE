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

    @Column
    private String title;

    @Builder
    private Member(Long kakaoId, String nickname, String email, String password, String title) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.title = title;
    }

    public Member kakaoIdUpdate(Long kakaoId) {
        this.kakaoId = kakaoId;
        return this;
    }

    public void updateStudyTime(Long roomStudyTime) {
        if (this.dailyStudyTime == null) {
            this.dailyStudyTime = 0L;
        }
        this.dailyStudyTime += roomStudyTime;
    }

    public void updateTotalStudyTime() {
        this.totalStudyTime += this.dailyStudyTime;
        this.dailyStudyTime = 0L;
        updateTitle();
    }

    private void updateTitle() {
        if (this.totalStudyTime >= 1000 * 60 * 60) {
            this.title = "Lv5";
        } else if (this.totalStudyTime >= 501 * 60 * 60) {
            this.title = "Lv4";
        } else if (this.totalStudyTime >= 201 * 60 * 60) {
            this.title = "Lv3";
        } else if (this.totalStudyTime >= 51 * 60 * 60) {
            this.title = "Lv2";
        } else {
            this.title = "Lv1";
        }
    }
}