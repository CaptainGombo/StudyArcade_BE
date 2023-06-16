package trillion9.studyarcade_be.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByKakaoId(Long id);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findById(Long id);
    @Query(value = "SELECT m.nickname, m.title, m.total_study_time FROM member as m ORDER BY m.total_study_time DESC LIMIT 1", nativeQuery = true)
    List<Object[]> findTopRanked();
}