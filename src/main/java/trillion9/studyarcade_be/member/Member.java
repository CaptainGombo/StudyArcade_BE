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

    private Long dailyStudyTime;
    private Long totalStudyTime;
    private String title;
    private String imageUrl;

    @Builder
    private Member(Long kakaoId, String nickname, String email, String password, Long dailyStudyTime, Long totalStudyTime, String title, String imageUrl) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.dailyStudyTime = dailyStudyTime;
        this.totalStudyTime = totalStudyTime;
        this.title = title != null ? title : "씨앗";
        this.imageUrl = imageUrl;
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
        updateTitle();
    }

    private void updateTitle() {
        if (this.totalStudyTime >= 1501 * 60) {
            this.title = "세계수";
        } else if (this.totalStudyTime >= 1001 * 60) {
            this.title = "백년나무";
        } else if (this.totalStudyTime >= 651 * 60) {
            this.title = "거목";
        } else if (this.totalStudyTime >= 401 * 60) {
            this.title = "나무";
        } else if (this.totalStudyTime >= 201 * 60) {
            this.title = "묘목";
        } else if (this.totalStudyTime >= 51 * 60) {
            this.title = "새싹";
        } else {
            this.title = "씨앗";
        }
    }


    public void updateMember(MemberRequestDto memberRequestDto, String imageUrl) {
        this.nickname = memberRequestDto.getNickname();
        this.imageUrl = imageUrl;
    }
}