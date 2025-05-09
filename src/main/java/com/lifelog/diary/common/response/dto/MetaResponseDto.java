package com.lifelog.diary.common.response.dto;

import com.lifelog.diary.common.response.enums.Code;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MetaResponseDto {

    private int status;
    private final String code;
    private final String message;

    public MetaResponseDto(Code code, String customMessage) {
        this.status = code.getStatus();
        this.code = code.getCode();
        this.message = code.getMessage(customMessage);
    }
}
