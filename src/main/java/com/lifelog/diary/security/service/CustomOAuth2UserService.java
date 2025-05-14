package com.lifelog.diary.security.service;

import com.lifelog.diary.domain.User;
import com.lifelog.diary.domain.enums.AuthProvider;
import com.lifelog.diary.domain.enums.Role;
import com.lifelog.diary.repository.UserRepository;
import com.lifelog.diary.security.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2ResDto oAuth2ResDto = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디 값을 만듦
        String username = oAuth2ResDto.getProvider() + "_" + oAuth2ResDto.getProviderId();
        User existUserData = userRepository.findByUsername(username);

        // 한번도 로그인 하지 않아 유저 정보가 db에 존재하지 않는 경우
        if (existUserData == null) {

            User user = User.createUser(
                    AuthProvider.GOOGLE,
                    oAuth2ResDto.getProviderId(),
                    oAuth2ResDto.getEmail(),
                    username,
                    Role.USER,
                    false
            );

            userRepository.save(user);

            UserDto userDto = UserDto.builder()
                    .username(username)
                    .name(oAuth2ResDto.getName())
                    .email(oAuth2ResDto.getEmail())
                    .role(Role.USER)
                    .build();

            return new CustomOAuth2User(userDto);
        }
        // 한번이라도 로그인 해서 유저 정보가 존재하는 경우
        else {

            // 정보 업데이트
            existUserData.updateSocialInfo(oAuth2ResDto.getEmail());

            userRepository.save(existUserData);

            UserDto userDto = UserDto.builder()
                    .username(existUserData.getUsername())
                    .name(oAuth2ResDto.getName())
                    .email(existUserData.getEmail())
                    .role(existUserData.getRole())
                    .build();

            return new CustomOAuth2User(userDto);

        }
    }
}
