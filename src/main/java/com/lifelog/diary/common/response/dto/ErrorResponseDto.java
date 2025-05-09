package com.lifelog.diary.common.response.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorResponseDto<T> {

    private final MetaResponseDto meta;
    private final List<T> data;

}
