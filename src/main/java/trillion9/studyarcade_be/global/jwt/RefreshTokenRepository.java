package trillion9.studyarcade_be.global.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import trillion9.studyarcade_be.member.Member;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
}
