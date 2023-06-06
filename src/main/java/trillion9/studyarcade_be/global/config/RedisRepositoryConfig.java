package trillion9.studyarcade_be.global.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableRedisRepositories
public class RedisRepositoryConfig {

	private final RedisProperties redisProperties;
	// lettuce
	// RedisConnectionFactory 인터페이스를 통해 LettuceConnectionFactory를 생성하여 반환한다.
	// RedisProperties로 yaml에 저장한 host, post를 가지고 와서 연결한다.
	@Bean
	public RedisConnectionFactory redisConnectionFactory(){
		return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
	}

	// 단일 Topic 사용을 위한 Bean 설정
	@Bean
	public ChannelTopic channelTopic() {
		return new ChannelTopic("studyroom");
	}


	/**
	 * 실제 메시지를 처리하는 subscriber 설정 추가
	 */
	@Bean
	public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
		return new MessageListenerAdapter(subscriber, "sendMessage");
	}

	/**
	 * redis에 발행(publish)된 메시지 처리를 위한 리스너 설정
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter,
		ChannelTopic channelTopic) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, channelTopic);
		return container;
	}

	// setKeySerializer, setValueSerializer 설정으로 redis-cli를 통해 직접 데이터를 보는게 가능함.
	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		return redisTemplate;
	}


}