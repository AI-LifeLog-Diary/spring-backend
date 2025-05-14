package com.lifelog.diary.controller;

import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.dto.ResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.dto.UserJoinReqDto;
import com.lifelog.diary.dto.UserJoinResDto;
import com.lifelog.diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping ("/join")
    public ResponseDto<UserJoinResDto> join(@RequestPart("userInfo") UserJoinReqDto userJoinReqDto,
                                            @RequestPart("profileImage") MultipartFile profileImage) {
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "회원가입이 성공적으로 완료되었습니다.");
        UserJoinResDto data = userService.join(userJoinReqDto, profileImage);
        return new ResponseDto<>(meta, Collections.singletonList(data));
    }

}
