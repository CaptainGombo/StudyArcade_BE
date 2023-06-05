package trillion9.studyarcade_be.room.dto;

import javax.validation.constraints.NotBlank;import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RoomCreateRequestDto {

	@NotBlank(message = "스터디룸 이름을 입력해 주세요!")
	private String roomName;

	@NotBlank(message = "스터디룸 설명을 입력해 주세요!")
	private String roomContent;

	// @NotBlank(message = "카테고리를 선택해 주세요!")
	// private String category;

	 private boolean secret;

	 private String roomPassword;

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