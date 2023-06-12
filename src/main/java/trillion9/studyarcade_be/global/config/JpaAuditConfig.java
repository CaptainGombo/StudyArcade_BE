package trillion9.studyarcade_be.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import trillion9.studyarcade_be.global.security.UserDetailsImpl;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return Optional.of(userDetails.getMember().getId());
        }
        return Optional.empty();
    }
}
