package com.lifelog.diary.common.response.exception;

import com.lifelog.diary.common.response.dto.ErrorResponseDto;
import com.lifelog.diary.common.response.dto.MetaResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(GeneralException e) {
        MetaResponseDto meta = new MetaResponseDto(e.getCode(), e.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(meta, Collections.emptyList());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(e.getCode().getStatus()));
    }

}