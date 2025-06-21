package com.lifelog.diary.security.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelog.diary.common.response.dto.ErrorResponseDto;
import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.security.converter.AppleTokenResponseClient;
import com.lifelog.diary.security.interceptor.OAuth2RequestUriFilter;
import com.lifelog.diary.security.jwt.JWTFilter;
import com.lifelog.diary.security.jwt.JWTUtil;
import com.lifelog.diary.security.oauth2.CustomSuccessHandler;
import com.lifelog.diary.security.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.Collections;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;
    private final AppleTokenResponseClient appleTokenResponseClient;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF / Form / HTTP Basic 비활성화
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 세션 사용 안 함 (JWT 기반 무상태 인증)
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 로그인
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .tokenEndpoint(token -> token.accessTokenResponseClient(customTokenResponseClient())) // () 괄호로 호출!
                .successHandler(customSuccessHandler)
        );

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {

                    MetaResponseDto meta = new MetaResponseDto(Code.INVALID_TOKEN, "엑세스 토큰이 유효하지 않거나 존재하지 않습니다.");
                    ErrorResponseDto<Object> error = new ErrorResponseDto<>(meta, Collections.emptyList());

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    ObjectMapper objectMapper = new ObjectMapper();
                    response.getWriter().write(objectMapper.writeValueAsString(error));
                })
        );

        // JWT 필터 등록 (모든 인증 요청 전에 실행되도록 설정)
        http
                .addFilterBefore(new OAuth2RequestUriFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 경로별 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/login/**", "/auth/refresh/**","/oauth2/**", "/public/**", "/api/auth/**", "dialogue/chat-request").permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> customTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient defaultClient = new DefaultAuthorizationCodeTokenResponseClient();

        return request -> {
            String registrationId = request.getClientRegistration().getRegistrationId();
            if ("apple".equalsIgnoreCase(registrationId)) {
                return appleTokenResponseClient.getTokenResponse(request);
            }
            return defaultClient.getTokenResponse(request);
        };
    }

}
