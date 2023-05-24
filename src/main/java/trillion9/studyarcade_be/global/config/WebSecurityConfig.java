package trillion9.studyarcade_be.global.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import trillion9.studyarcade_be.global.jwt.JwtAuthFilter;
import trillion9.studyarcade_be.global.jwt.JwtUtil;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {
    private final JwtUtil jwtUtil;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // resources 접근 허용 설정
        return web -> web.ignoring()
                //static 파일들
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                //Swagger
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**","/api-docs","/swagger-ui.html");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeHttpRequests()
                //회원가입, 로그인페이지, 메인 페이지
                .antMatchers("/members/login").permitAll()
                .antMatchers("/members/signup").permitAll()
                .antMatchers("/emails/**").permitAll()
                .antMatchers(HttpMethod.GET,"/posts/**").permitAll()

                .anyRequest().authenticated()
                // JWT 인증/인가를 사용하기 위한 설정
                .and().addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 이 설정을 해주지 않으면 밑의 cors가 적용되지 않는다
        http.cors();

        // 401 Error 처리, Authorization 즉, 인증과정에서 실패할 시 처리
        //http.exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint);

        return http.build();
    }

//      이렇게 Spring Security만 사용해서 CORS를 적용할 수 있습니다
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource(){
//
//        CorsConfiguration config = new CorsConfiguration();
//
//        config.addAllowedOriginPattern("*");
//
//        // 특정 헤더를 클라이언트 측에서 사용할 수 있게 지정
//        // 만약 지정하지 않는다면, Authorization 헤더 내의 토큰 값을 사용할 수 없음
//        config.addExposedHeader(JwtUtil.ACCESS_TOKEN);
//        config.addExposedHeader(JwtUtil.REFRESH_TOKEN);
//
//        // 본 요청에 허용할 HTTP method(예비 요청에 대한 응답 헤더에 추가됨)
//        config.addAllowedMethod("*");
//
//        // 본 요청에 허용할 HTTP header(예비 요청에 대한 응답 헤더에 추가됨)
//        config.addAllowedHeader("*");
//
//        // 기본적으로 브라우저에서 인증 관련 정보들을 요청 헤더에 담지 않음
//        // 이 설정을 통해서 브라우저에서 인증 관련 정보들을 요청 헤더에 담을 수 있도록 해줍니다.
//        config.setAllowCredentials(true);
//
//        // allowCredentials 를 true로 하였을 때,
//        // allowedOrigin의 값이 * (즉, 모두 허용)이 설정될 수 없도록 검증합니다.
//        config.validateAllowCredentials();
//
//        // 어떤 경로에 이 설정을 적용할 지 명시합니다. (여기서는 전체 경로)
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        return source;
//    }

}
