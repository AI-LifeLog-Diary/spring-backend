package com.lifelog.diary.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelog.diary.common.response.dto.ErrorResponseDto;
import com.lifelog.diary.common.response.dto.MetaResponseDto;
import com.lifelog.diary.common.response.enums.Code;
import com.lifelog.diary.domain.enums.Role;
import com.lifelog.diary.security.dto.CustomOAuth2User;
import com.lifelog.diary.security.dto.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        // 1. 토큰이 없으면 인증 처리 없이 다음 필터로 넘어감
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 토큰 만료 확인
        if (jwtUtil.isExpired(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            setErrorResponse(response, "엑세스 토큰이 만료되었습니다.");
            return;
        }

        // 3. 토큰에서 사용자 정보 추출
        String username = jwtUtil.getUsername(token);
        Role role = jwtUtil.getRole(token);

        // 4. 사용자 인증 객체 생성
        UserDto userDto = UserDto.builder()
                .username(username)
                .role(role)
                .build();

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, customOAuth2User.getAuthorities());

        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 5. 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    // 토큰 추출 (Authorization 헤더)
    private String extractToken(HttpServletRequest request) {
        //Authorization 헤더 검사 (Bearer 토큰)
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void setErrorResponse(HttpServletResponse response, String message) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        MetaResponseDto meta = new MetaResponseDto(Code.UNAUTHORIZED, message);
        ErrorResponseDto<Object> error = new ErrorResponseDto<>(meta, Collections.emptyList());

        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}