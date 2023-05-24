package trillion9.studyarcade_be.member;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
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
}
