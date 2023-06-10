package trillion9.studyarcade_be.room.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class RoomCreateRequestDto {
	@NotBlank(message = "스터디룸 이름을 입력해 주세요!")
	private String roomName;
	@NotBlank(message = "스터디룸 설명을 입력해 주세요!")
	private String roomContent;

	private String category;
	private boolean secret;
	private String roomPassword;
	private LocalDate expirationDate;

	// @AssertTrue
	// public boolean isCategory() {
	// 	try{
	// 		CategoryEnum categoryEnum = CategoryEnum.valueOf(category);
	// 	} catch (Exception exception){
	// 		throw new IllegalArgumentException("category 값을 정확하게 입력해 주세요.");
	// 	}
	// 	return true;
	//
	// }
}