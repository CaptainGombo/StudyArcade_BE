package trillion9.studyarcade_be.global;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import trillion9.studyarcade_be.global.exception.CustomException;
import trillion9.studyarcade_be.global.exception.ErrorCode;
import trillion9.studyarcade_be.global.jwt.JwtUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
	private final JwtUtil jwtUtil;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		String accessToken = "";
		String refreshToken = "";

		if(accessor.getCommand() == StompCommand.CONNECT) {
			log.info("소켓 Connect JWT 확인");
			accessToken = accessor.getFirstNativeHeader("Access_Token");
			refreshToken = accessor.getFirstNativeHeader("Refresh_Token");

			String jwtAccessToken = jwtUtil.socketResolveToken(accessToken);

			/* Access Token 검증 성공인 경우 */
			if (jwtAccessToken != null && jwtUtil.validateToken(jwtAccessToken)) {
				log.info("엑세스 토큰 인증 성공");
			}
			/* Access 토큰이 만료된 경우 또는 유효하지 않은 경우 */
			else {
				log.info("JWT 토큰이 만료되어, Refresh token 확인 작업을 진행합니다.");

				/* Refresh Token 존재 여부 확인.*/
				String jwtRefreshToken = jwtUtil.resolveToken(refreshToken);

				/* Refresh Token이 유효한 경우 */
				if (jwtRefreshToken != null && jwtUtil.validateToken(jwtRefreshToken)) {
					log.info("리프레시 토큰 인증 성공");
				} else {
					throw new CustomException(ErrorCode.INVALID_TOKEN);
				}
			}
		}
		return message;
	}
}