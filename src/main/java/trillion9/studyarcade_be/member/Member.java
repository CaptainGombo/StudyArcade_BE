package trillion9.studyarcade_be.member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trillion9.studyarcade_be.member.dto.MemberRequestDto;

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

    @Column
    private String imageUrl;

    @Builder
    private Member(Long kakaoId, String nickname, String email, String password, String title, String imageUrl) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.title = title != null ? title : "Lv1";
        this.imageUrl = imageUrl;
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

    public void updateMember(MemberRequestDto memberRequestDto, String imageUrl) {
        this.nickname = memberRequestDto.getNickname();
        this.imageUrl = imageUrl;
    }
}