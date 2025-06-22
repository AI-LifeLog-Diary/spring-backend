package com.lifelog.diary.service;

import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.domain.UserHobby;
import com.lifelog.diary.dto.*;
import com.lifelog.diary.image.service.ImageService;
import com.lifelog.diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;


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

            user.updateProfile(
                    userJoinReqDto.getNickname(),
                    userJoinReqDto.getBirth(),
                    userJoinReqDto.getGender(),
                    profileUrl,
                    userJoinReqDto.getHobbyList(),
                    true
            );

            userRepository.save(user);

            return UserJoinResDto.builder()
                    .userId(user.getId())
                    .createdAt(user.getCreatedAt())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralException(Code.INTERNAL_ERROR, "회원 정보 저장 도중 알 수 없는 오류가 발생했습니다");
        }
    }

    public UserProfileResDto getProfile() {
        try {
            User currentUser;
            try {
                currentUser = accountService.getCurrentUser();
            } catch (Exception e) {
                throw new GeneralException(Code.USER_NOT_FOUND, "현재 인증된 회원이 존재하지 않습니다.");
            }

            String profileUrl = currentUser.getProfileUrl();
            String presignedUrl = imageService.generatePresignedUrlFromFullUrl(profileUrl);

            return UserProfileResDto.builder()
                    .userId(currentUser.getId())
                    .authProvider(currentUser.getProvider())
                    .email(currentUser.getEmail())
                    .username(currentUser.getUsername())
                    .nickname(currentUser.getNickname())
                    .birth(currentUser.getBirth())
                    .role(currentUser.getRole())
                    .gender(currentUser.getGender())
                    .profileUrl(presignedUrl)
                    .hobbyList(currentUser.getHobbyList().stream()
                            .map(UserHobby::getHobby)
                            .collect(Collectors.toList()))
                    .createdAt(currentUser.getCreatedAt())
                    .build();

        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_ERROR, "프로필 조회 도중 알 수 없는 에러가 발생했습니다.");
        }

    }

    public UserProfileUpdateResDto updateProfile(UserProfileUpdateReqDto userProfileUpdateReqDto, MultipartFile profileImage) {

        User currentUser;
        try {
            currentUser = accountService.getCurrentUser();
        } catch (Exception e) {
            throw new GeneralException(Code.USER_NOT_FOUND, "현재 인증된 회원이 존재하지 않습니다.");
        }

        if (userProfileUpdateReqDto == null && profileImage.isEmpty()) {
            throw new GeneralException(Code.INVALID_INPUT_VALUE, "프로필 수정 요청 정보가 존재하지 않습니다.");
        }

        String presignedUrl;
        String profileUrl = currentUser.getProfileUrl();
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                profileUrl = imageService.uploadImageToS3(profileImage);
            }
            presignedUrl = imageService.generatePresignedUrlFromFullUrl(profileUrl);
            System.out.println(presignedUrl);
        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_ERROR, "프로필 이미지 업데이트 중 에러가 발생하였습니다.");
        }

        //TODO : 변경된 필드 있는지 여부 검증하는 로직 추가


        try {

            if (userProfileUpdateReqDto!=null) {
                currentUser.updateProfile(
                        userProfileUpdateReqDto.getNickname(),
                        userProfileUpdateReqDto.getBirth(),
                        userProfileUpdateReqDto.getGender(),
                        profileUrl,
                        userProfileUpdateReqDto.getHobbyList(),
                        true
                );
            }


            return UserProfileUpdateResDto.builder()
                    .userId(currentUser.getId())
                    .authProvider(String.valueOf(currentUser.getProvider()))
                    .email(currentUser.getEmail())
                    .username(currentUser.getUsername())
                    .nickname(currentUser.getNickname())
                    .birth(currentUser.getBirth())
                    .role(String.valueOf(currentUser.getRole()))
                    .gender(String.valueOf(currentUser.getGender()))
                    .profileUrl(presignedUrl)
                    .hobbyList(currentUser.getHobbyList().stream()
                            .map(UserHobby::getHobby)
                            .collect(Collectors.toList()))
                    .createdAt(currentUser.getCreatedAt())
                    .updatedAt(currentUser.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_ERROR, "프로필 업데이트 중 오류가 발생하였습니다.");
        }
    }

    public void deleteUser() {

        try {
            User currentUser;
            try {
                currentUser = accountService.getCurrentUser();
            } catch (Exception e) {
                throw new GeneralException(Code.USER_NOT_FOUND, "현재 인증된 회원이 존재하지 않습니다.");
            }


            if (currentUser == null) {
                throw new AccessDeniedException("로그인한 사용자만 탈퇴할 수 있습니다.");
            }

            try {
                userRepository.delete(currentUser); // 정책에 따라 soft delete 방식으로 변경할지 고민
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("계정에 연결된 데이터로 인해 삭제할 수 없습니다.");
            }
        } catch (Exception e) {
            throw new GeneralException(Code.INTERNAL_ERROR, "회원 탈퇴 도중 알 수 없는 에러가 발생했습니다.");
        }

    }

    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}


