package com.lifelog.diary.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.domain.enums.Role;
import com.lifelog.diary.repository.UserRepository;
import com.lifelog.diary.security.converter.AppleTokenResponseClient;
import com.lifelog.diary.security.dto.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes;

        if ("apple".equals(registrationId)) {
            String idToken = userRequest.getAdditionalParameters().get("id_token").toString();

            if (idToken==null) {
                throw new OAuth2AuthenticationException("id token이 존재하지 않습니다.");
            }

            // 1. 헤더에서 kid 추출
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) throw new OAuth2AuthenticationException("잘못된 JWT 구조입니다.");

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);

            Map header;

            try {
                header = new ObjectMapper().readValue(headerJson, Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String kid = (String) header.get("kid");

            // 2. 공개키 가져오기
            PublicKey publicKey;
            try {
                publicKey = AppleTokenResponseClient.getApplePublicKey(kid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 3. 검증 및 파싱
            Jws<Claims> parsedJwt = Jwts.parser()
                    .json(new JacksonDeserializer<>())
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken);

            Claims claims = parsedJwt.getPayload();

            attributes = new HashMap<>();
            attributes.put("provider", "apple");
            attributes.put("sub", claims.getSubject());
            attributes.put("email", claims.get("email"));
            attributes.put("name", claims.get("name")); // nullable

        } else {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            attributes = oAuth2User.getAttributes();
        }

        OAuth2ResDto oAuth2ResDto = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디 값을 만듦
        String username = oAuth2ResDto.getProvider() + "_" + oAuth2ResDto.getProviderId();
        User existUserData = userRepository.findByUsername(username);

        // 한번도 로그인 하지 않아 유저 정보가 db에 존재하지 않는 경우
        if (existUserData == null) {

            User user = User.createUser(
                    oAuth2ResDto.getProvider(),
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

            return new CustomOAuth2User(userDto, true);
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

            User user = userRepository.findByUsername(username);
            boolean isNewUser = !user.isProfileCompleted();
            return new CustomOAuth2User(userDto, isNewUser);
        }
    }
}
