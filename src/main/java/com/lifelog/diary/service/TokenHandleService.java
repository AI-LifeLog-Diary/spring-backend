package com.lifelog.diary.service;

import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.common.response.exception.GeneralException;
import com.lifelog.diary.domain.User;
import com.lifelog.diary.dto.TokenResDto;
import com.lifelog.diary.repository.UserRepository;
import com.lifelog.diary.security.jwt.JWTUtil;
import com.lifelog.diary.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenHandleService {

    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    private final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000L;           // 1시간
    private final long REFRESH_TOKEN_EXPIRATION = 14 * 24 * 60 * 60 * 1000L; // 14일

    public TokenResDto verifyTokenAndGenerateToken(String refreshToken) {

        String username = jwtUtil.getUsername(refreshToken);

        // 1. Redis에서 저장된 Refresh Token 가져오기
        String storedRefreshToken = refreshTokenService.getRefreshToken(username);

        // 2. 저장된 Refresh Token과 클라이언트에서 전달된 Refresh Token 비교
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {

            User user = userRepository.findByUsername(username);

            // 3. 새로운 Access Token 발급
            String newAccessToken = jwtUtil.createAccessToken(username, user.getRole(), ACCESS_TOKEN_EXPIRATION);
            // 4. 새로운 Refresh Token 발급
            String newRefreshToken = jwtUtil.createRefreshToken(username, user.getRole(), REFRESH_TOKEN_EXPIRATION);

            // 5. 기존 Refresh Token 삭제 및 새로운 Refresh Token 저장
            refreshTokenService.deleteRefreshToken(username);
            refreshTokenService.storeRefreshToken(username, newRefreshToken, REFRESH_TOKEN_EXPIRATION);

            return TokenResDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } else {
            throw new GeneralException(Code.UNAUTHORIZED, "Refresh Token이 유효하지 않거나 만료되었습니다.");
        }
    }
}
