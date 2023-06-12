package trillion9.studyarcade_be.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import trillion9.studyarcade_be.global.StompHandler;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final StompHandler stompHandler; // jwt 인증

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// 메시지를 구독하는 요청 url => 즉 메시지 받을 때
		config.enableSimpleBroker("/sub");
		// 메시지를 발행하는 요청 url => 즉 메시지 보낼 때
		config.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// stomp 접속 주소 url => /ws-stomp
		registry.addEndpoint("/ws") // 연결될 엔드포인트
			.setAllowedOriginPatterns("*")
			.withSockJS(); // SocketJS 를 연결한다는 설정
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setMessageSizeLimit(50 * 1024 * 1024);
	}

	 @Override
	 public void configureClientInboundChannel(ChannelRegistration registration) {
	 	registration.interceptors(stompHandler);
	 }
}