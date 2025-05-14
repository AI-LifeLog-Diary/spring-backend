package com.lifelog.diary.common.response.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Code {

    /*

    1. Sxxx : 성공 응답
	2. Exxx : 실패 응답

     */

    // 성공 응답 (Sxxx)
    OK(HttpStatus.OK.value(),"S001", "요청이 성공하였습니다."),

    // 클라이언트 오류 (ECxxx)
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "EC001", "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "EC002", "요청한 리소스를 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "EC003", "유효하지 않은 값을 입력하였습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST.value(), "EC004", "파일 크기가 허용된 한도를 초과했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST.value(), "EC005", "허용되지 않은 파일 형식입니다."),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "EC006", "업로드된 파일을 찾을 수 없습니다."),
    INVALID_INPUT_FORMAT(HttpStatus.BAD_REQUEST.value(), "EC007", "유효하지 않은 형식의 요청입니다."),
    MEMBER_MISMATCH(HttpStatus.BAD_REQUEST.value(), "EC008", "요청한 회원 정보가 일치하지 않습니다."),

    // 서버 오류 (ESxxx),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ES001", "예기치 못한 서버 에러가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ES002", "데이터베이스 처리 중 오류가 발생했습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ES003", "파일 업로드 중 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ES004", "외부 API 호출 중 오류가 발생했습니다."),

    // 인증,인가 오류 (EUxxx)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "EU001", "인증되지 않은 사용자입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "EU002", "유효하지 않은 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "EU003", "접근이 허용되지 않았습니다."),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "EU004", "존재하지 않는 회원입니다."),

    /*비즈니스 오류 (EBxxx)*/
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "EB001", "존재하지 않는 세션입니다.");

    private final int status;
    private final String code;
    @Getter
    private final String message;


    public String getMessage(String customMessage) {
        return this.message + " - " + customMessage;
    }
}