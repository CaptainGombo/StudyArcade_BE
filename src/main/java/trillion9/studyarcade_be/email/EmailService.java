package trillion9.studyarcade_be.email;

import trillion9.studyarcade_be.global.ResponseDto;

public interface EmailService {

    ResponseDto<Object> sendSimpleMessage(String to)throws Exception;
}