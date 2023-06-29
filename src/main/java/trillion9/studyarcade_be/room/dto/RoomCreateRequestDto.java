package trillion9.studyarcade_be.room.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Builder
@Getter
public class RoomCreateRequestDto {
	@NotBlank(message = "스터디룸 이름을 입력해 주세요!")
	private String roomName;
	@NotBlank(message = "스터디룸 설명을 입력해 주세요!")
	private String roomContent;

	private String category;
	private boolean secret;
	private String roomPassword;
	private LocalDate expirationDate;

}