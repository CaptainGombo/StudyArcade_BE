package trillion9.studyarcade_be.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.member.MemberRepository;

import static trillion9.studyarcade_be.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return new UserDetailsImpl(member, member.getEmail());
    }
}
