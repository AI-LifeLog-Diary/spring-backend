package com.lifelog.diary.controller;

import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.dto.ResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.TokenReqDto;
import com.lifelog.diary.dto.TokenResDto;
import com.lifelog.diary.service.AccountService;
import com.lifelog.diary.service.TokenHandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final TokenHandleService tokenHandleService;

    @PostMapping("/refresh")
    public ResponseDto<TokenResDto> refresh(@RequestBody TokenReqDto tokenReqDto) {
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "토큰이 성공적으로 발급되었습니다.");
        TokenResDto data = tokenHandleService.verifyTokenAndGenerateToken(tokenReqDto.getRefreshToken());
        return new ResponseDto<>(meta, Collections.singletonList(data));
    }
}
