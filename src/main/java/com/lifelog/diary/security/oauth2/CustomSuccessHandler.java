package com.lifelog.diary.security.oauth2;

import com.lifelog.diary.domain.enums.Role;
import com.lifelog.diary.security.dto.CustomOAuth2User;
import com.lifelog.diary.security.jwt.JWTUtil;
import com.lifelog.diary.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // OAuth2UserCustom
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        // jwt username 값 필요
        String username = customUserDetails.getUserDto().getUsername();

        // jwt role 값 필요
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        // Access Token (1시간)
        String accessToken = jwtUtil.createAccessToken(username, Role.valueOf(role), 60 * 60 * 1000L);

        // Refresh Token (14일)
        String refreshToken = jwtUtil.createRefreshToken(username, Role.valueOf(role), 60 * 60 * 24 * 14 * 1000L);

        // Redis에 Refresh Token 저장
        refreshTokenService.storeRefreshToken(username, refreshToken, 60 * 60 * 24 * 14 * 1000L);

        // 클라이언트로 JSON 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        boolean isNewUser = customUserDetails.isNewUser();

        String schemePrefix = (String) request.getSession().getAttribute("dynamic_redirect_uri");
        if (schemePrefix == null || schemePrefix.isBlank()) {
            schemePrefix = "";
        }

        String finalRedirect = String.format(
                "%s://oauth2/callback?accessToken=%s&refreshToken=%s&isNewUser=%b",
                schemePrefix,
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8),
                isNewUser
        );

        response.sendRedirect(finalRedirect);
        System.out.println(finalRedirect);
    }
}
