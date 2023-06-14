package trillion9.studyarcade_be.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByKakaoId(Long id);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findById(Long id);
    List<Member> findAllByOrderByTotalStudyTimeDesc();
}
