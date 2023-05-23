package trillion9.studyarcade_be.global.jwt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.resolveToken(request, JwtUtil.ACCESS_TOKEN);
        String refreshToken = jwtUtil.resolveToken(request, JwtUtil.REFRESH_TOKEN);
        if(accessToken != null) {
            //Access 토큰 유효 시, security context에 인증 정보 저장
            if(jwtUtil.validateToken(accessToken)){
                setAuthentication(jwtUtil.getUserInfoFromToken(accessToken));
            }
            // Access 토큰 만료
            else if (refreshToken != null) {
                // Refresh 토큰 유효
                if (Boolean.TRUE.equals(jwtUtil.validateRefreshToken(refreshToken))){
                    String userEmail = jwtUtil.getUserInfoFromToken(refreshToken);
                    //new accessToken 발급
                    String newAccessToken = jwtUtil.createToken(userEmail, JwtUtil.ACCESS_TOKEN);
                    //헤더에 새로운 Access 토큰 넣기
                    response.setHeader(JwtUtil.ACCESS_TOKEN,newAccessToken);
                    //Security context에 인증 정보 저장
                    String newToken = newAccessToken.substring(7);
                    setAuthentication(jwtUtil.getUserInfoFromToken(newToken));
                    log.info("새로운 토큰 생성 완료");
                }
                //Access & Refresh 토큰 만료시
                else {
                    throw new IllegalArgumentException("Token이 없습니다");
                }
            }
        }
        filterChain.doFilter(request,response);
    }

    public void setAuthentication(String userEmail) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtUtil.createAuthentication(userEmail);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
