package com.lifelog.diary.common.response.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {

    private final MetaResponseDto meta;
    private List<T> data;

    public ResponseDto(MetaResponseDto meta, List<T> data) {
        this.meta = meta;
        this.data = data != null ? data : new ArrayList<>();
    }

}
