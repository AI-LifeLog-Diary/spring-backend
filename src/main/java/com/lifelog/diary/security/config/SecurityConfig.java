package com.lifelog.diary.security.config;


import com.lifelog.diary.security.jwt.JWTFilter;
import com.lifelog.diary.security.jwt.JWTUtil;
import com.lifelog.diary.security.oauth2.CustomSuccessHandler;
import com.lifelog.diary.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CORS 설정
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:3000")); // (임시) 프론트 주소
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(false); // 쿠키 사용 x
            config.setMaxAge(3600L);
            config.setExposedHeaders(List.of("Authorization")); // JWT 헤더 노출 허용
            return config;
        }));

        // CSRF / Form / HTTP Basic 비활성화
        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // 세션 사용 안 함 (JWT 기반 무상태 인증)
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 로그인
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
        );

        // JWT 필터 등록 (모든 인증 요청 전에 실행되도록 설정)
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 경로별 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login","/auth/refresh","/oauth2/**", "/public/**", "/api/auth/**").permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }
}
