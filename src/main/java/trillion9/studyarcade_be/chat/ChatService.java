package trillion9.studyarcade_be.chat;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import trillion9.studyarcade_be.member.Member;
import trillion9.studyarcade_be.member.MemberRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic channelTopic;
	private final MemberRepository memberRepository;

	public void message(ChatMessageDto message) {

		// 프로필 이미지 설정
		String profile = retrieveProfileImage(message.getNickname());
		message.setProfile(profile);

		// Websocket에 발행된 메시지를 redis로 발행 (publish)
		redisTemplate.convertAndSend(channelTopic.getTopic(), message);
	}

	private String retrieveProfileImage(String nickname) {

		Optional<Member> member = memberRepository.findByNickname(nickname);

		if (member.isPresent() && member.get().getImageUrl() != null) {
			return member.get().getImageUrl();
		}

		return "대표 프로필 이미지 URL";
	}
}