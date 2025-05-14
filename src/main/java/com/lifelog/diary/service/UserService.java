package com.lifelog.diary.service;

import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.UserJoinReqDto;
import com.lifelog.diary.dto.UserJoinResDto;
import com.lifelog.diary.image.service.ImageService;
import com.lifelog.diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final AccountService accountService;

    public UserJoinResDto join(UserJoinReqDto userJoinReqDto, MultipartFile profileImage) {

        if (isNicknameExists(userJoinReqDto.getNickname())) {
            throw new GeneralException(Code.INVALID_INPUT_VALUE, "중복된 닉네임입니다.");
        }

        String profileUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileUrl = imageService.uploadImageToS3(profileImage);
        }

        try {

            User user = accountService.getCurrentUser();

            user.updateUserInfo(
                    userJoinReqDto.getNickname(),
                    userJoinReqDto.getBirth(),
                    userJoinReqDto.getGender(),
                    profileUrl,
                    true
            );
            user.updateHobbies(userJoinReqDto.getHobbyList());

            userRepository.save(user);

            return UserJoinResDto.builder()
                    .userId(user.getId())
                    .createdAt(user.getCreatedAt())
                    .build();

        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에서 구체적인 에러 확인
            throw new GeneralException(Code.INTERNAL_ERROR, "회원 정보 저장 도중 알 수 없는 오류가 발생했습니다");
        }
    }


    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}


