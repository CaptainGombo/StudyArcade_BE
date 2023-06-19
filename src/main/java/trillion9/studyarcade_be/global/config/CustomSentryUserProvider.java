package trillion9.studyarcade_be.global.config;

import io.sentry.protocol.User;
import io.sentry.spring.SentryUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import trillion9.studyarcade_be.global.security.UserDetailsImpl;

@Component
class CustomSentryUserProvider implements SentryUserProvider {

    @Override
    public User provideUser() {
        User user = new User();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            user.setId(userDetails.getMember().getId().toString());
            user.setEmail(userDetails.getMember().getEmail());
            user.setUsername(userDetails.getMember().getNickname());
        }
        return user;
    }
}