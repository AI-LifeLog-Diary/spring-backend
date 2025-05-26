package com.lifelog.diary.controller;

import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.dto.ResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.dto.*;
import com.lifelog.diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseDto<UserJoinResDto> join(@RequestPart("userInfo") UserJoinReqDto userJoinReqDto,
                                            @RequestPart("profileImage") MultipartFile profileImage) {
        UserJoinResDto data = userService.join(userJoinReqDto, profileImage);
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "회원가입이 성공적으로 완료되었습니다.");
        return new ResponseDto<>(meta, Collections.singletonList(data));
    }

    @GetMapping("/join/nickname-check")
    public ResponseDto<Boolean> verifyDuplicateNickname(@RequestParam("nickname") String nickname) {
        boolean isExists = userService.isNicknameExists(nickname);
        if (!isExists) {
            MetaResponseDto meta = new MetaResponseDto(Code.OK, "사용 가능한 닉네임입니다.");
            return new ResponseDto<>(meta, Collections.emptyList());
        } else {
            MetaResponseDto meta = new MetaResponseDto(Code.OK, "이미 사용 중인 닉네임입니다.");
            return new ResponseDto<>(meta, Collections.emptyList());
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseDto<UserProfileResDto> getProfile(@PathVariable("userId") Long userId) throws AccessDeniedException {
        UserProfileResDto data = userService.getProfile(userId);
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "사용자 프로필을 성공적으로 조회하였습니다.");
        return new ResponseDto<>(meta, Collections.singletonList(data));
    }

    @PatchMapping("/profile/{userId}/update")
    public ResponseDto<UserProfileUpdateResDto> updateProfile(@PathVariable("userId") Long userId,
                                                              @RequestBody UserProfileUpdateReqDto userProfileUpdateReqDto) throws AccessDeniedException {
        UserProfileUpdateResDto data = userService.updateProfile(userId, userProfileUpdateReqDto);
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "사용자 프로필을 성공적으로 수정하였습니다.");
        return new ResponseDto<>(meta, Collections.singletonList(data));
    }

    @DeleteMapping("/profile/delete")
    public ResponseDto<Void> updateProfile() throws AccessDeniedException {
        MetaResponseDto meta = new MetaResponseDto(Code.OK, "사용자 탈퇴가 성공적으로 수행되었습니다.");
        userService.deleteUser();
        return new ResponseDto<>(meta, Collections.emptyList());
    }


}
