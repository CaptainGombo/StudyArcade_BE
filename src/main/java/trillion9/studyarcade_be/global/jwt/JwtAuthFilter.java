package trillion9.studyarcade_be.global.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

//        if (true) throw new CustomException(ErrorCode.INVALID_TOKEN);
        String accessToken = jwtUtil.resolveToken(request, JwtUtil.ACCESS_TOKEN);
        String refreshToken = jwtUtil.resolveToken(request, JwtUtil.REFRESH_TOKEN);

        if(accessToken != null) {
            //Access 토큰 유효 시, security context에 인증 정보 저장
            if(jwtUtil.validateToken(accessToken)) {
                // Redis에 해당 accessToken logout 여부를 확인
                String isLogout = redisTemplate.opsForValue().get("BL:" + accessToken);
                // 로그아웃이 없는(되어 있지 않은) 경우 해당 토큰은 정상적으로 작동하기
                if (ObjectUtils.isEmpty(isLogout)) {
                    setAuthentication(jwtUtil.getUserInfoFromToken(accessToken));
                }
            } else {
                jwtExceptionHandler(response, "Access Token Expired", HttpStatus.FORBIDDEN.value());
                return;
            }

//            // Access 토큰 만료 & Refresh 토큰 유효
//            else if (refreshToken != null && Boolean.TRUE.equals(jwtUtil.validateRefreshToken(refreshToken))) {
//                String userEmail = jwtUtil.getUserInfoFromToken(refreshToken);
//                //new accessToken 발급
//                String newAccessToken = jwtUtil.createToken(userEmail, JwtUtil.ACCESS_TOKEN);
//                //헤더에 새로운 Access 토큰 넣기
//                response.setHeader(JwtUtil.ACCESS_TOKEN, newAccessToken);
//                //Security context에 인증 정보 저장
//                String newToken = newAccessToken.substring(7);
//                setAuthentication(jwtUtil.getUserInfoFromToken(newToken));
//                log.info("New Token Issued");
//
//            } else if (refreshToken == null) {
//                jwtExceptionHandler(response, "Access Token Expired", HttpStatus.FORBIDDEN.value());
//                return;
//            } else {
//                //Access & Refresh 토큰 만료시
//                jwtExceptionHandler(response, "Access & Refresh Token Expired", HttpStatus.FORBIDDEN.value());
//                return;
//            }
        }
        filterChain.doFilter(request,response);
    }

    public void setAuthentication(String userEmail) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtUtil.createAuthentication(userEmail);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    public void jwtExceptionHandler(HttpServletResponse response, String msg, int statusCode) {
        response.setStatus(statusCode);
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        try {
            response.getWriter().write(msg);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
